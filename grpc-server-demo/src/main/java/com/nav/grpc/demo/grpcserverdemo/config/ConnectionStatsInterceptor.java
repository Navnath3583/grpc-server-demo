package com.nav.grpc.demo.grpcserverdemo.config;

import io.grpc.*;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionStatsInterceptor implements ServerInterceptor {

    private final AtomicInteger activeCalls = new AtomicInteger(0);

    public int getActiveCallCount() {
        return activeCalls.get();
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        activeCalls.incrementAndGet();
        System.out.println("Active calls-->"+ activeCalls.get());

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onComplete() {
                activeCalls.decrementAndGet();
                super.onComplete();
            }

            @Override
            public void onCancel() {
                activeCalls.decrementAndGet();
                super.onCancel();
            }
        };
    }
}
