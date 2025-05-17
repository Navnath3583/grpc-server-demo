package com.nav.grpc.demo.grpcclientdemo.service;

import com.nav.grpc.demo.grpcclientdemo.client.RouteGuideClient;
import com.nav.grpc.demo.grpcclientdemo.model.RouteGuideRequest;
import com.nav.grpc.demo.msg.Feature;
import com.nav.grpc.demo.msg.Point;
import com.nav.grpc.demo.msg.Rectangle;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;

@Service
@AllArgsConstructor
public class RouteGuideServerStreaming {

    private RouteGuideClient routeGuideClientImpl;

    /*public void getFeatures(RouteGuideRequest routeGuideRequest) {
        Rectangle request =
                Rectangle.newBuilder()
                        .setLo(Point.newBuilder().setLatitude(0).setLongitude(0).build())
                        .setHi(Point.newBuilder().setLatitude(0).setLongitude(0).build()).build();
        Iterator<Feature> features;
        try {
            features = routeGuideClient.getRouteGuideBlockingStub().listFeatures(request);
            for (int i = 1; features.hasNext(); i++) {
                Feature feature = features.next();
                System.out.println("Feature-->" + feature);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void getFeatures(RouteGuideRequest routeGuideRequest) {
        Rectangle request =
                Rectangle.newBuilder()
                        .setLo(Point.newBuilder().setLatitude(0).setLongitude(0).build())
                        .setHi(Point.newBuilder().setLatitude(0).setLongitude(0).build()).build();
        Iterator<Feature> features;
        try {
            routeGuideClientImpl.getAsyncStub().listFeatures(request, new StreamObserver<Feature>() {
                @Override
                public void onNext(Feature feature) {
                    System.out.println("Feature-->" + feature);
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    System.out.println("Successful");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
