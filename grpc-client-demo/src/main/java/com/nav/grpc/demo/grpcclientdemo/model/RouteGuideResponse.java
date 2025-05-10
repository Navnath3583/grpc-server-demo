package com.nav.grpc.demo.grpcclientdemo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteGuideResponse {
    private String name;
    private Point point;

}

@Getter
@Setter
class Point {
    private int latitude;
    private int longitude;
}
