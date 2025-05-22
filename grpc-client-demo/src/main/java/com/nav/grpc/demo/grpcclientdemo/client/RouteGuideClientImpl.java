package com.nav.grpc.demo.grpcclientdemo.client;

import com.nav.grpc.demo.grpcclientdemo.config.RouteGuideNameResolverProvider;
import com.nav.grpc.demo.grpcclientdemo.config.JwtCredential;
import com.nav.grpc.demo.msg.RouteGuideGrpc;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service
@Getter
@Profile("!custom-load-balancer")
public class RouteGuideClientImpl implements RouteGuideClient {

    private static String clientId = "default-client";
    @Value("${grpc.connection.pool:1}")
    private int collectionPool;
    private RouteGuideGrpc.RouteGuideStub routeGuideAsyncStub;
    private RouteGuideGrpc.RouteGuideBlockingStub routeGuideBlockingStub;
    private CallCredentials credentials;
    private List<ManagedChannel> channels;
    private List<RouteGuideGrpc.RouteGuideBlockingStub> listOfBlockingStubs;
    private List<RouteGuideGrpc.RouteGuideStub> listOfAsyncStubs;
    private Random rand = new Random();


    @Override
    public void startClient() throws IOException {
        channels = new ArrayList<>();
        listOfBlockingStubs = new ArrayList<>();
        listOfAsyncStubs = new ArrayList<>();
        NameResolverRegistry.getDefaultRegistry().register(new RouteGuideNameResolverProvider());
        String target = "example:///lb.example.grpc.io";
        SslContext clientSslContext = GrpcSslContexts.forClient()
                .trustManager(ResourceUtils.getFile("classpath:route_guide/ca.pem"))
                /*.keyManager(ResourceUtils.getFile("classpath:route_guide/client.pem"),
                        ResourceUtils.getFile("classpath:route_guide/client.key"))*/
                .build();
        IntStream.range(0, this.collectionPool).mapToObj(index -> {
            try {
                return NettyChannelBuilder
                        .forTarget(target)
                        .usePlaintext()
                        //.sslContext(clientSslContext)
                        // Change hostname to match certificate
                        .overrideAuthority("192.168.1.3")
                        .defaultLoadBalancingPolicy("round_robin")
                        //.intercept(new StreamIdInterceptor())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).forEach(channel -> {
            channels.add(channel);
            listOfBlockingStubs.add(RouteGuideGrpc.newBlockingStub(channel));
            listOfAsyncStubs.add(RouteGuideGrpc.newStub(channel));
        });
        ManagedChannel channel = NettyChannelBuilder
                .forTarget(target)
                //.usePlaintext()
                .sslContext(clientSslContext)
                // Change hostname to match certificate
                .overrideAuthority("192.168.1.3")
                .defaultLoadBalancingPolicy("round_robin")
                //.intercept(new StreamIdInterceptor())
                .build();
        this.credentials = new JwtCredential(clientId);
        this.routeGuideBlockingStub = RouteGuideGrpc.newBlockingStub(channel);
        this.routeGuideAsyncStub = RouteGuideGrpc.newStub(channel);
        System.out.println("Client started");
    }

    public RouteGuideGrpc.RouteGuideStub getNextAsyncStub() {
        return listOfAsyncStubs.get(rand.nextInt(this.collectionPool));
        //return this.routeGuideAsyncStub;
    }

    public RouteGuideGrpc.RouteGuideBlockingStub getNextBlockingStub() {
        return listOfBlockingStubs.get(rand.nextInt(this.collectionPool));
        //return this.routeGuideBlockingStub;
    }

    public void shutdown() {
        for (ManagedChannel channel : channels) {
            channel.shutdown();
        }
    }
}
