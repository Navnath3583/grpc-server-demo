package com.nav.grpc.demo.grpcserverdemo.server;

import com.nav.grpc.demo.grpcserverdemo.service.RouteGuideService;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
@Component
public class RouteGuideServer {
    private static final Logger log = LoggerFactory.getLogger(RouteGuideServer.class);

    private RouteGuideService routeGuideService;

    public void start() throws IOException, InterruptedException {
        Server server = Grpc.newServerBuilderForPort(50051, InsecureServerCredentials.create())
                .addService(routeGuideService)
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
