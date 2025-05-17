package com.nav.grpc.demo.grpcclientdemo.config;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import java.net.URI;

public class RouteGuideNameResolverProvider extends NameResolverProvider {
    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

    @Override
    public NameResolver newNameResolver(URI uri, NameResolver.Args args) {
        return new RouteGuideNameResolver(uri);
    }

    @Override
    public String getDefaultScheme() {
        return "example";
    }
}
