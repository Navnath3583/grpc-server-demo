package com.nav.grpc.demo.grpcclientdemo.controller;

import com.nav.grpc.demo.grpcclientdemo.model.RouteGuideRequest;
import com.nav.grpc.demo.grpcclientdemo.service.RouteGuideBiDiStreaming;
import com.nav.grpc.demo.grpcclientdemo.service.RouteGuideClientStreaming;
import com.nav.grpc.demo.grpcclientdemo.service.RouteGuideServerStreaming;
import com.nav.grpc.demo.grpcclientdemo.service.RouteGuideUnary;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class DemoController {

    RouteGuideUnary routeGuideUnary;
    RouteGuideServerStreaming routeGuideServerStreaming;
    RouteGuideClientStreaming routeGuideClientStreaming;
    RouteGuideBiDiStreaming routeGuideBiDiStreaming;

    @PostMapping(value = "/unary-features", consumes = "application/json")
    public String getUnaryFeature(@RequestBody RouteGuideRequest routeGuideRequest) {
        routeGuideUnary.getFeature(routeGuideRequest);
        return "Success";
    }

    @PostMapping(value = "/server-streaming-features", consumes = "application/json")
    public String getServerStreamingFeatures(@RequestBody RouteGuideRequest routeGuideRequest) {
        routeGuideServerStreaming.getFeatures(routeGuideRequest);
        return "Success";
    }

    @PostMapping(value = "/client-streaming-features", consumes = "application/json")
    public String getClientStreamingFeatures(@RequestBody RouteGuideRequest routeGuideRequest) {
        routeGuideClientStreaming.getFeatures(routeGuideRequest);
        return "Success";
    }

    @PostMapping(value = "/bidi-streaming-features", consumes = "application/json")
    public String getBiDiStreamingFeatures(@RequestBody RouteGuideRequest routeGuideRequest) {
        routeGuideBiDiStreaming.getFeatures(routeGuideRequest);
        return "Success";
    }
}
