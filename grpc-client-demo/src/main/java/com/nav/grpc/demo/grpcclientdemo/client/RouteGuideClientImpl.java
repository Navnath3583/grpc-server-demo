package com.nav.grpc.demo.grpcclientdemo.client;

import com.nav.grpc.demo.grpcclientdemo.config.RouteGuideNameResolverProvider;
import com.nav.grpc.demo.grpcclientdemo.config.JwtCredential;
import com.nav.grpc.demo.msg.RouteGuideGrpc;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;

@Service
@Getter
@Profile("!custom-load-balancer")
public class RouteGuideClientImpl implements RouteGuideClient {

    RouteGuideGrpc.RouteGuideStub asyncStub;
    RouteGuideGrpc.RouteGuideBlockingStub routeGuideBlockingStub;
    String clientId = "default-client";
    CallCredentials credentials;

    @Override
    public void startClient() throws IOException {
        NameResolverRegistry.getDefaultRegistry().register(new RouteGuideNameResolverProvider());
        String target = "example:///lb.example.grpc.io";

        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                .sslContext(
                        GrpcSslContexts.forClient()
                                .trustManager(ResourceUtils.getFile("classpath:route_guide/ca.pem"))
                                .keyManager(ResourceUtils.getFile("classpath:route_guide/client.pem"),
                                        ResourceUtils.getFile("classpath:route_guide/client.key"))
                                .build())
                // Change hostname to match certificate
                .overrideAuthority("192.168.1.3")
                .defaultLoadBalancingPolicy("round_robin")
                .build();
        this.credentials = new JwtCredential(clientId);
        this.asyncStub = RouteGuideGrpc.newStub(channel);
        this.routeGuideBlockingStub = RouteGuideGrpc.newBlockingStub(channel);
        System.out.println("Client started");
    }
}
