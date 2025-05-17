package com.nav.grpc.demo.grpcclientdemo.config;

import io.grpc.LoadBalancer;
import io.grpc.LoadBalancerProvider;
import io.grpc.NameResolver;
import io.grpc.Status;

import java.util.Map;

public class ShufflingPickFirstLoadBalancerProvider extends LoadBalancerProvider {
    @Override
    public NameResolver.ConfigOrError parseLoadBalancingPolicyConfig(Map<String, ?> rawLoadBalancingPolicyConfig) {
        Long randomSeed = null;
        try {
            Object randomSeedObj = rawLoadBalancingPolicyConfig.get("randomSeed");
            if (randomSeedObj instanceof Double) {
                randomSeed = ((Double) randomSeedObj).longValue();
            }
            return NameResolver.ConfigOrError.fromConfig(new ShufflingPickFirstLoadBalancer.Config(randomSeed));
        } catch (RuntimeException e) {
            return NameResolver.ConfigOrError.fromError(
                    Status.UNAVAILABLE.withDescription("unable to parse LB config").withCause(e));
        }
    }

    @Override
    public LoadBalancer newLoadBalancer(LoadBalancer.Helper helper) {
        return new ShufflingPickFirstLoadBalancer(helper);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public String getPolicyName() {
        return "ShufflingPickFirst";
    }
}
