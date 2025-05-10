package com.nav.grpc.demo.grpcserverdemo.service;

import com.nav.grpc.demo.grpcserverdemo.util.RouteGuideUtil;
import com.nav.grpc.demo.msg.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RouteGuideService extends RouteGuideGrpc.RouteGuideImplBase {
    List<Feature> features;
    private final ConcurrentMap<Point, List<RouteNote>> routeNotes =
            new ConcurrentHashMap<>();

    public RouteGuideService() throws IOException {
        this.features = RouteGuideUtil.getDefaultFeaturesList();
    }

    @Override
    public void getFeature(Point request, StreamObserver<Feature> responseObserver) {
        responseObserver.onNext(this.features.getFirst());
        responseObserver.onCompleted();
    }

    @Override
    public void listFeatures(Rectangle request, StreamObserver<Feature> responseObserver) {
        int left = Math.min(request.getLo().getLongitude(), request.getHi().getLongitude());
        int right = Math.max(request.getLo().getLongitude(), request.getHi().getLongitude());
        int top = Math.max(request.getLo().getLatitude(), request.getHi().getLatitude());
        int bottom = Math.min(request.getLo().getLatitude(), request.getHi().getLatitude());

        for (Feature feature : features) {
            if (!RouteGuideUtil.exists(feature)) {
                continue;
            }

            int lat = feature.getLocation().getLatitude();
            int lon = feature.getLocation().getLongitude();
            if (lon >= left && lon <= right && lat >= bottom && lat <= top) {
                responseObserver.onNext(feature);
            }
            responseObserver.onNext(feature);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Point> recordRoute(StreamObserver<RouteSummary> responseObserver) {
        return new StreamObserver<Point>() {
            int pointCount;

            @Override
            public void onNext(Point point) {
                pointCount++;
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(RouteSummary.newBuilder().setPointCount(pointCount)
                        .setFeatureCount(0).setDistance(0)
                        .setElapsedTime((int) 1).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<RouteNote> routeChat(StreamObserver<RouteNote> responseObserver) {
        return new StreamObserver<RouteNote>() {
            @Override
            public void onNext(RouteNote routeNote) {
                System.out.println("<------------- CLient sent the request --->");
                List<RouteNote> notes = getOrCreateNotes(routeNote.getLocation());
                // Respond with all previous notes at this location.
                responseObserver.onNext(RouteNote.newBuilder()
                        .setMessage("Server Response")
                        .setLocation(Point.newBuilder().setLatitude(10)
                                .setLongitude(10).build()).build());
                for (RouteNote prevNote : notes.toArray(new RouteNote[0])) {
                    responseObserver.onNext(prevNote);
                }
                // Now add the new note to the list
                notes.add(routeNote);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
                System.out.println("<----------Client Finished RouteChat---------->");
            }
        };
    }

    private List<RouteNote> getOrCreateNotes(Point location) {
        List<RouteNote> notes = Collections.synchronizedList(new ArrayList<>());
        List<RouteNote> prevNotes = routeNotes.putIfAbsent(location, notes);
        return prevNotes != null ? prevNotes : notes;
    }
}
