package com.nav.grpc.demo.grpcclientdemo.service;

import com.nav.grpc.demo.grpcclientdemo.client.RouteGuideClient;
import com.nav.grpc.demo.grpcclientdemo.model.RouteGuideRequest;
import com.nav.grpc.demo.msg.Feature;
import com.nav.grpc.demo.msg.Point;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RouteGuideUnary {

    private RouteGuideClient routeGuideClientImpl;

    /*
    This is the Sync Stub Implementation
     */
    /*public void getFeature(RouteGuideRequest routeGuideRequest) {
        Point point = Point.newBuilder().setLatitude(Integer.parseInt(routeGuideRequest.getLatitude()))
                .setLongitude(Integer.parseInt(routeGuideRequest.getLongitude()))
                .build();
        Feature feature = routeGuideBlockingStub.getFeature(point);
        System.out.println("Response-->" + feature);
    }*/

    /*
    This is the aSync Stub Implementation
     */
    public void getFeature(RouteGuideRequest routeGuideRequest) {
        Point point = Point.newBuilder().setLatitude(Integer.parseInt(routeGuideRequest.getLatitude()))
                .setLongitude(Integer.parseInt(routeGuideRequest.getLongitude()))
                .build();
        routeGuideClientImpl.getAsyncStub().withCallCredentials(routeGuideClientImpl.getCredentials())
                .getFeature(point, new StreamObserver<Feature>() {
                    @Override
                    public void onNext(Feature feature) {
                        System.out.println("Feature-->" + feature);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Status status = Status.fromThrowable(throwable);
                        Metadata trailers = Status.trailersFromThrowable(throwable);
                        System.out.println("Server error: Code: " + status.getCode() +
                                "Message: " + status.getDescription());
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Successful");
                    }
                });
    }
}
