package com.nav.grpc.demo.grpcserverdemo.server;

import com.nav.grpc.demo.grpcserverdemo.service.RouteGuideService;
import io.grpc.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
@AllArgsConstructor
@Component
public class RouteGuideServer {
    private RouteGuideService routeGuideService;

    public void start() throws IOException, InterruptedException {
        ServerCredentials serverCredentials = TlsServerCredentials.newBuilder()
                .keyManager(ResourceUtils.getFile("classpath:route_guide/server1.pem"),
                        ResourceUtils.getFile("classpath:route_guide/server1.key"))
                .trustManager(ResourceUtils.getFile("classpath:route_guide/ca.pem"))
                .clientAuth(TlsServerCredentials.ClientAuth.REQUIRE)
                .build();

        Server server = Grpc.newServerBuilderForPort(50051, serverCredentials)
                .addService(routeGuideService)
                .intercept(new JwtServerInterceptor())
                .build();
        server.start();
        log.info("Server started, listening on " + 50051);
        blockUntilShutdown(server);
    }

    private void blockUntilShutdown(Server server) throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
