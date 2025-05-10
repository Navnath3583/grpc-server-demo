package com.nav.grpc.demo.grpcclientdemo;

import com.nav.grpc.demo.grpcclientdemo.client.RouteGuideClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class GrpcClientDemoApplication {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(GrpcClientDemoApplication.class, args);
        applicationContext.getBean(RouteGuideClient.class)
                .startClient();
    }

}
