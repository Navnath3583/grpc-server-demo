package com.nav.grpc.demo.grpcserverdemo.server;

import com.nav.grpc.demo.grpcserverdemo.service.RouteGuideService;
import io.grpc.*;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RouteGuideServer {
    @Value("${grpc.server.port}")
    private int serverPort;

    @Autowired
    private RouteGuideService routeGuideService;

    public void start() throws IOException, InterruptedException {
        /*ServerCredentials serverCredentials = TlsServerCredentials.newBuilder()
                .keyManager(ResourceUtils.getFile("classpath:route_guide/server1.pem"),
                        ResourceUtils.getFile("classpath:route_guide/server1.key"))
                .trustManager(ResourceUtils.getFile("classpath:route_guide/ca.pem"))
                .clientAuth(TlsServerCredentials.ClientAuth.REQUIRE)
                .build();*/

        HealthStatusManager healthStatusManager = new HealthStatusManager();

        SslContext serverSslContext = GrpcSslContexts.forServer(ResourceUtils.getFile("classpath:route_guide/server1.pem"),
                        ResourceUtils.getFile("classpath:route_guide/server1.key"))
                .trustManager(ResourceUtils.getFile("classpath:route_guide/ca.pem"))
                .clientAuth(ClientAuth.NONE)
                .build();
        Server server = NettyServerBuilder.forPort(this.serverPort)
                .sslContext(serverSslContext)
                .addService(routeGuideService)
                .addService(ProtoReflectionServiceV1.newInstance())
                .addService(healthStatusManager.getHealthService())
                //.intercept(new JwtServerInterceptor())
                .build()
                .start();
        log.info("Server started, listening on " + this.serverPort);

        /*
        Code to handle Graceful Shutdown of server. Server will wait for 30 seconds to all RPC to complete.
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //Start graceful shutdown
            server.shutdown();
            try {
                //Wait for 30 seconds for RPC's to get complete
                server.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            server.shutdownNow();
        }));
        healthStatusManager.setStatus("", HealthCheckResponse.ServingStatus.SERVING);
        server.awaitTermination();
    }
}
