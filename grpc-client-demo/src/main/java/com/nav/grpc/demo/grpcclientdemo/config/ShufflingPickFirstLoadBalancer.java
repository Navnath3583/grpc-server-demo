package com.nav.grpc.demo.grpcclientdemo.config;

import com.google.common.base.MoreObjects;
import io.grpc.*;
import lombok.Lombok;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShufflingPickFirstLoadBalancer extends LoadBalancer {
    private final Helper helper;
    private Subchannel subchannel;

    static class Config {
        final Long randomSeed;

        public Config(Long randomSeed) {
            this.randomSeed = randomSeed;
        }
    }

    @Override
    public void handleResolvedAddresses(ResolvedAddresses resolvedAddresses) {
        List<EquivalentAddressGroup> servers = new ArrayList<>(resolvedAddresses.getAddresses());
        if (servers.isEmpty()) {
            Status unavailableStatus = Status.UNAVAILABLE.withDescription(
                    "NameResolver returned no usable address. addrs=" + resolvedAddresses.getAddresses()
                            + ", attrs=" + resolvedAddresses.getAttributes());
            handleNameResolutionError(unavailableStatus);
        }

        Config config
                = (Config) resolvedAddresses.getLoadBalancingPolicyConfig();
        Collections.shuffle(servers,
                config.randomSeed != null ? new Random(config.randomSeed) : new Random());
        if (subchannel == null) {
            final Subchannel subchannel = helper.createSubchannel(
                    CreateSubchannelArgs.newBuilder()
                            .setAddresses(servers)
                            .build());
            subchannel.start(new SubchannelStateListener() {
                @Override
                public void onSubchannelState(ConnectivityStateInfo stateInfo) {
                    processSubchannelState(subchannel, stateInfo);
                }
            });
            this.subchannel = subchannel;
            helper.updateBalancingState(ConnectivityState.CONNECTING, new Picker(PickResult.withNoResult()));
            subchannel.requestConnection();
        } else {
            subchannel.updateAddresses(servers);
        }
    }

    private void processSubchannelState(Subchannel subchannel, ConnectivityStateInfo stateInfo) {
        ConnectivityState currentState = stateInfo.getState();
        if (currentState == ConnectivityState.SHUTDOWN) {
            return;
        }
        if (stateInfo.getState() == ConnectivityState.TRANSIENT_FAILURE || stateInfo.getState() == ConnectivityState.IDLE) {
            helper.refreshNameResolution();
        }
        SubchannelPicker picker;
        switch (currentState) {
            case IDLE:
                picker = new RequestConnectionPicker(subchannel);
                break;
            case CONNECTING:
                picker = new Picker(PickResult.withNoResult());
                break;
            case READY:
                picker = new Picker(PickResult.withSubchannel(subchannel));
                break;
            case TRANSIENT_FAILURE:
                picker = new Picker(PickResult.withError(stateInfo.getStatus()));
                break;
            default:
                throw new IllegalArgumentException("Unsupported state:" + currentState);
        }
        helper.updateBalancingState(currentState, picker);
    }

    public ShufflingPickFirstLoadBalancer(Helper helper) {
        this.helper = helper;
    }

    @Override
    public void requestConnection() {
        if (subchannel != null) {
            subchannel.requestConnection();
        }
    }

    @Override
    public void handleNameResolutionError(Status status) {
        if (subchannel != null) {
            subchannel.shutdown();
            subchannel = null;
        }
        helper.updateBalancingState(ConnectivityState.TRANSIENT_FAILURE, new Picker(PickResult.withError(status)));
    }

    @Override
    public void shutdown() {
        if (subchannel != null) {
            subchannel.shutdown();
        }
    }

    private static final class Picker extends SubchannelPicker {
        private final PickResult pickResult;

        public Picker(PickResult pickResult) {
            this.pickResult = pickResult;
        }

        @Override
        public PickResult pickSubchannel(PickSubchannelArgs pickSubchannelArgs) {
            return pickResult;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Picker.class).add("result", pickResult).toString();
        }
    }

    private final class RequestConnectionPicker extends SubchannelPicker {
        private final Subchannel subchannel;
        private final AtomicBoolean connectionRequested = new AtomicBoolean(false);

        RequestConnectionPicker(Subchannel subchannel) {
            this.subchannel = Lombok.checkNotNull(subchannel, "subchannel");
        }

        @Override
        public PickResult pickSubchannel(PickSubchannelArgs pickSubchannelArgs) {
            if (connectionRequested.compareAndSet(false, true)) {
                helper.getSynchronizationContext().execute(new Runnable() {
                    @Override
                    public void run() {
                        subchannel.requestConnection();
                    }
                });
            }
            return PickResult.withNoResult();
        }
    }
}
