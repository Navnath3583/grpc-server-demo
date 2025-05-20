package com.nav.grpc.demo.grpcclientdemo.config;

import io.grpc.*;
import io.grpc.stub.StreamObserver;

public class StreamIdInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> methodDescriptor,
            CallOptions callOptions,
            Channel next) {

        // Create a custom client call with logging
        ClientCall<ReqT, RespT> call = next.newCall(methodDescriptor, callOptions);

        // Add custom handling logic before invoking the actual call
        return new ClientCall<ReqT, RespT>() {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Log the stream id or any relevant metadata
                System.out.println("Starting call to method: " + methodDescriptor.getFullMethodName());
                System.out.println("Call headers: " + headers);
                // Start the call with the response listener
                call.start(responseListener, headers);
            }

            @Override
            public void request(int numMessages) {
                call.request(numMessages);
            }

            @Override
            public void cancel(String message, Throwable cause) {
                call.cancel(message, cause);
            }

            @Override
            public void sendMessage(ReqT message) {
                call.sendMessage(message);
            }

            @Override
            public void halfClose() {
                call.halfClose();
            }

            @Override
            public void setMessageCompression(boolean enabled) {
                call.setMessageCompression(enabled);
            }
        };
    }
}

