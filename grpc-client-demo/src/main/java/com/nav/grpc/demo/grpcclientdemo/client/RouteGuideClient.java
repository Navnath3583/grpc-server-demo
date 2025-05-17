package com.nav.grpc.demo.grpcclientdemo.client;

import com.nav.grpc.demo.msg.RouteGuideGrpc;
import io.grpc.CallCredentials;

import java.io.IOException;

public interface RouteGuideClient {

    void startClient() throws IOException;

    RouteGuideGrpc.RouteGuideStub getAsyncStub();

    CallCredentials getCredentials();
}
