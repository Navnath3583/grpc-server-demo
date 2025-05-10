package com.nav.grpc.demo.grpcserverdemo.service;

import com.nav.grpc.demo.grpcserverdemo.util.RouteGuideUtil;
import com.nav.grpc.demo.msg.Feature;
import com.nav.grpc.demo.msg.Point;
import com.nav.grpc.demo.msg.RouteGuideGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class RouteGuideService extends RouteGuideGrpc.RouteGuideImplBase {
    List<Feature> features;

    public RouteGuideService() throws IOException {
        this.features = RouteGuideUtil.getDefaultFeaturesList();
    }

    @Override
    public void getFeature(Point request, StreamObserver<Feature> responseObserver) {
        responseObserver.onNext(this.features.getFirst());
        responseObserver.onCompleted();
    }
}
