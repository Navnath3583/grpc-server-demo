package com.nav.grpc.demo.grpcclientdemo.service;

import com.nav.grpc.demo.grpcclientdemo.client.RouteGuideClient;
import com.nav.grpc.demo.grpcclientdemo.model.RouteGuideRequest;
import com.nav.grpc.demo.msg.Point;
import com.nav.grpc.demo.msg.RouteSummary;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RouteGuideClientStreaming {

    private RouteGuideClient routeGuideClientImpl;

    public void getFeatures(RouteGuideRequest routeGuideRequest) {
        Point request = Point.newBuilder().setLatitude(0).setLongitude(0).build();
        try {
            StreamObserver<Point> pointStreamObserver = routeGuideClientImpl.getNextAsyncStub().recordRoute(new StreamObserver<RouteSummary>() {
                @Override
                public void onNext(RouteSummary routeSummary) {
                    System.out.println("Route Summary-->" + routeSummary);
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    System.out.println("Completed");
                }
            });
            for (int i = 0; i < 10; i++) {
                pointStreamObserver.onNext(request);
            }
            pointStreamObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
