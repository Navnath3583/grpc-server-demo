package com.nav.grpc.demo.grpcclientdemo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteGuideRequest {
    private String latitude;
    private String longitude;
}
