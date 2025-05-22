package com.nav.grpc.demo.grpcclientdemo.client;

import com.google.gson.Gson;
import com.nav.grpc.demo.grpcclientdemo.config.RouteGuideNameResolverProvider;
import com.nav.grpc.demo.grpcclientdemo.config.JwtCredential;
import com.nav.grpc.demo.grpcclientdemo.config.ShufflingPickFirstLoadBalancerProvider;
import com.nav.grpc.demo.msg.RouteGuideGrpc;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.Map;

@Service
@Getter
@Profile("custom-load-balancer")
public class RouteGuideClientCustomLBImpl implements RouteGuideClient {

    private RouteGuideGrpc.RouteGuideStub routeGuideAsyncStub;
    private RouteGuideGrpc.RouteGuideBlockingStub routeGuideBlockingStub;
    RouteGuideGrpc.RouteGuideStub asyncStub;
    String clientId = "default-client";
    CallCredentials credentials;

    @Override
    public void startClient() throws IOException {
        // We need to register the provider of our custom load balancer implementation
        LoadBalancerRegistry.getDefaultRegistry()
                .register(new ShufflingPickFirstLoadBalancerProvider());
        NameResolverRegistry.getDefaultRegistry().register(new RouteGuideNameResolverProvider());

        String target = "example:///lb.example.grpc.io";
        System.out.println("Use default first_pick load balance policy");

        Map<String, ?> serviceConfig = new Gson().fromJson(
                "{ \"loadBalancingConfig\": " +
                        "    [ { \"ShufflingPickFirst\": { \"randomSeed\": 2 } } ]" +
                        "}",
                Map.class);

        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                .sslContext(
                        GrpcSslContexts.forClient()
                                .trustManager(ResourceUtils.getFile("classpath:route_guide/ca.pem"))
                                .keyManager(ResourceUtils.getFile("classpath:route_guide/client.pem"),
                                        ResourceUtils.getFile("classpath:route_guide/client.key"))
                                .build())
                .defaultServiceConfig(serviceConfig)
                //.idleTimeout(20, TimeUnit.SECONDS)
                // Change hostname to match certificate
                .overrideAuthority("192.168.1.3")
                .build();

        this.credentials = new JwtCredential(clientId);
        this.asyncStub = RouteGuideGrpc.newStub(channel);
        this.routeGuideBlockingStub = RouteGuideGrpc.newBlockingStub(channel);
        System.out.println("Client started");
    }

    @Override
    public RouteGuideGrpc.RouteGuideStub getNextAsyncStub() {
        return asyncStub;
    }

    @Override
    public RouteGuideGrpc.RouteGuideBlockingStub getNextBlockingStub() {
        return this.routeGuideBlockingStub;
    }
}
