package com.nav.grpc.demo.grpcserverdemo;

import com.nav.grpc.demo.grpcserverdemo.server.RouteGuideServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.IOException;

@SpringBootApplication
public class GrpcServerDemoApplication {

    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(GrpcServerDemoApplication.class, args);
        applicationContext.getBean(RouteGuideServer.class).start();
    }
}