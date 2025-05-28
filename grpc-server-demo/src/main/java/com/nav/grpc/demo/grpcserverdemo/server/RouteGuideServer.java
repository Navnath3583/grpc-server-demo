package com.nav.grpc.demo.grpcserverdemo.server;

import com.nav.grpc.demo.grpcserverdemo.service.RouteGuideService;
import io.grpc.*;
//import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
//import io.grpc.protobuf.services.HealthStatusManager;
//import io.grpc.protobuf.services.ProtoReflectionServiceV1;
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
        //Server server = simpleServer();
        Server server = serverWithKeepAliveSupport();
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
        //healthStatusManager.setStatus("", HealthCheckResponse.ServingStatus.SERVING);
        server.awaitTermination();
    }

    private Server simpleServer() throws IOException {
    /*ServerCredentials serverCredentials = TlsServerCredentials.newBuilder()
            .keyManager(ResourceUtils.getFile("classpath:route_guide/server1.pem"),
                    ResourceUtils.getFile("classpath:route_guide/server1.key"))
            .trustManager(ResourceUtils.getFile("classpath:route_guide/ca.pem"))
            .clientAuth(TlsServerCredentials.ClientAuth.REQUIRE)
            .build();*/

        //HealthStatusManager healthStatusManager = new HealthStatusManager();

        SslContext serverSslContext = GrpcSslContexts.forServer(ResourceUtils.getFile("classpath:route_guide/server1.pem"),
                        ResourceUtils.getFile("classpath:route_guide/server1.key"))
                .trustManager(ResourceUtils.getFile("classpath:route_guide/ca.pem"))
                .clientAuth(ClientAuth.NONE)
                .build();
        Server server = NettyServerBuilder.forPort(this.serverPort, InsecureServerCredentials.create())
                //.sslContext(serverSslContext)
                .addService(routeGuideService)
                //.addService(ProtoReflectionServiceV1.newInstance())
                //.addService(healthStatusManager.getHealthService())
                //.intercept(new JwtServerInterceptor())
                .build()
                .start();
        return server;
    }

    /*Start a server with the following configurations (demo only, you should set more appropriate
    values based on your real environment):
    keepAliveTime: Ping the client if it is idle for 5 seconds to ensure the connection is
    still active. Set to an appropriate value in reality, e.g. in minutes.
    keepAliveTimeout: Wait 1 second for the ping ack before assuming the connection is dead.
    Set to an appropriate value in reality, e.g. (10, TimeUnit.SECONDS).
    permitKeepAliveTime: If a client pings more than once every 5 seconds, terminate the
    connection.
    permitKeepAliveWithoutCalls: Allow pings even when there are no active streams.
    maxConnectionIdle: If a client is idle for 15 seconds, send a GOAWAY.
    maxConnectionAge: If any connection is alive for more than 30 seconds, send a GOAWAY.
    maxConnectionAgeGrace: Allow 5 seconds for pending RPCs to complete before forcibly closing
    connections.
    Use JAVA_OPTS=-Djava.util.logging.config.file=logging.properties to see keep alive ping
    frames.
    More details see: https://github.com/grpc/proposal/blob/master/A9-server-side-conn-mgt.md*/
    private Server serverWithKeepAliveSupport() throws IOException {
        return NettyServerBuilder.forPort(this.serverPort, InsecureServerCredentials.create())
                .addService(routeGuideService)
                .keepAliveTime(30, TimeUnit.SECONDS)
                .build()
                .start();
    }
}
