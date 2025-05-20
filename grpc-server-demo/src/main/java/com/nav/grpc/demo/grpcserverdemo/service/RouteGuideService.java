package com.nav.grpc.demo.grpcserverdemo.service;

import com.google.rpc.DebugInfo;
import com.nav.grpc.demo.grpcserverdemo.util.RouteGuideUtil;
import com.nav.grpc.demo.msg.*;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RouteGuideService extends RouteGuideGrpc.RouteGuideImplBase {
    @Autowired
    private ApplicationContext context;
    private static final Metadata.Key<DebugInfo> DEBUG_INFO_TRAILER_KEY =
            ProtoUtils.keyForProto(DebugInfo.getDefaultInstance());
    public static final DebugInfo DEBUG_INFO =
            DebugInfo.newBuilder()
                    .addStackEntries("stack_entry_1")
                    .addStackEntries("stack_entry_2")
                    .addStackEntries("stack_entry_3")
                    .setDetail("detailed error info").build();
    private static final String DEBUG_DESC = "detailed error description";

    List<Feature> features;
    private final ConcurrentMap<Point, List<RouteNote>> routeNotes =
            new ConcurrentHashMap<>();

    public RouteGuideService() throws IOException {
        this.features = RouteGuideUtil.getDefaultFeaturesList();
    }

    @Override
    public void getFeature(Point request, StreamObserver<Feature> responseObserver) {
        ServerCallStreamObserver serverCallStreamObserver = (ServerCallStreamObserver) responseObserver;
        serverCallStreamObserver.setOnCancelHandler(() -> {
            System.out.println("Client Cancelled the RPC");
        });
        //codeToValidateGracefulShutdown();
        //testExceptionScenario(responseObserver, serverCallStreamObserver);
        System.out.println("<---- Server received request--->");
        serverCallStreamObserver.onNext(this.features.getFirst());
        serverCallStreamObserver.onCompleted();
    }

    private static void codeToValidateGracefulShutdown() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                System.out.println("<----------------------System is exiting------------------------>");
                System.exit(129);
            }
        };
        thread.start();
        try {
            Thread.sleep(45000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void testExceptionScenario(StreamObserver<Feature> responseObserver, ServerCallStreamObserver serverCallStreamObserver) {
        Metadata trailers = new Metadata();
        trailers.put(DEBUG_INFO_TRAILER_KEY, DEBUG_INFO);
        responseObserver.onError(Status.INTERNAL.withDescription(DEBUG_DESC)
                .asRuntimeException(trailers));
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
                responseObserver.onError(Status.INTERNAL.withDescription("Server Error")
                        .asRuntimeException());
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
        //testOnCacelHandler((ServerCallStreamObserver) responseObserver);
        return new StreamObserver<RouteNote>() {
            @Override
            public void onNext(RouteNote routeNote) {
                System.out.println("<------------- CLient sent the request --->");
                responseObserver.onNext(RouteNote.newBuilder()
                        .setMessage("Server Response")
                        .setLocation(Point.newBuilder().setLatitude(10)
                                .setLongitude(10).build()).build());
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(Status.INTERNAL.withDescription("Server Error")
                        .asRuntimeException());
            }

            @Override
            public void onCompleted() {
                System.out.println("<----------Client Finished RouteChat---------->");
                responseObserver.onCompleted();
            }
        };
    }

    private static void testOnCacelHandler(ServerCallStreamObserver responseObserver) {
        ServerCallStreamObserver serverCallStreamObserver = responseObserver;
        serverCallStreamObserver.setOnCancelHandler(() -> {
            System.out.println("BiDi RPC Cancelled");
        });
    }

    private List<RouteNote> getOrCreateNotes(Point location) {
        List<RouteNote> notes = Collections.synchronizedList(new ArrayList<>());
        List<RouteNote> prevNotes = routeNotes.putIfAbsent(location, notes);
        return prevNotes != null ? prevNotes : notes;
    }
}
