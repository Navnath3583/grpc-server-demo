package com.nav.grpc.demo.grpcclientdemo.service;

import com.nav.grpc.demo.grpcclientdemo.client.RouteGuideClient;
import com.nav.grpc.demo.grpcclientdemo.model.RouteGuideRequest;
import com.nav.grpc.demo.msg.Point;
import com.nav.grpc.demo.msg.RouteNote;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
@AllArgsConstructor
public class RouteGuideBiDiStreaming {

    private RouteGuideClient routeGuideClientImpl;

    public void getFeatures(RouteGuideRequest routeGuideRequest) {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        ClientCallStreamObserver<RouteNote> requestObserver =
                (ClientCallStreamObserver<RouteNote>) routeGuideClientImpl
                        .getNextAsyncStub()
                        .routeChat(new StreamObserver<RouteNote>() {
                            @Override
                            public void onNext(RouteNote note) {
                                System.out.println("Got message {" + note.getMessage() + "}" + " at {" +
                                        note.getLocation().getLatitude() + "}" + ",{" + note.getLocation().getLongitude() + "}");
                                finishLatch.countDown();
                            }

                            @Override
                            public void onError(Throwable t) {
                                finishLatch.countDown();
                            }

                            @Override
                            public void onCompleted() {
                                System.out.println("<---------- Server Finished RouteChat ---------->");
                            }
                        });

        try {
            RouteNote[] requests =
                    {newNote("First message", 0, 0), newNote("Second message", 0, 10_000_000),
                            newNote("Third message", 10_000_000, 0)
                            , newNote("Fourth message", 10_000_000, 10_000_000)};
            RouteNote[] singleRequest =
                    {newNote("First message", 0, 0)};

            for (RouteNote request : singleRequest) {
                requestObserver.onNext(request);
            }
            finishLatch.await();
            requestObserver.onCompleted();
        } catch (Exception e) {
            requestObserver.onError(e);
        }

        // Mark the end of requests

        /*try {
            Thread.sleep(250); // Do some work
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }*/

        //requestObserver.cancel("I am done", null);
        /*try {
            Thread.sleep(100); // Make logs more obvious. Cancel is async
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
    }

    private RouteNote newNote(String message, int lat, int lon) {
        return RouteNote.newBuilder().setMessage(message)
                .setLocation(Point.newBuilder().setLatitude(lat).setLongitude(lon).build()).build();
    }
}
