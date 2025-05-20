package com.nav.grpc.demo.grpcserverdemo.util;

import com.google.protobuf.util.JsonFormat;
import com.nav.grpc.demo.msg.Feature;
import com.nav.grpc.demo.msg.FeatureDatabase;
import lombok.experimental.UtilityClass;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@UtilityClass
public class RouteGuideUtil {

    public static List<Feature> getDefaultFeaturesList() throws IOException {
        File file = ResourceUtils.getFile("classpath:route_guide/route_guide_db.json");
        try (FileInputStream inputStream = new FileInputStream(file)) {
            try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                FeatureDatabase.Builder database = FeatureDatabase.newBuilder();
                JsonFormat.parser().merge(reader, database);
                return database.getFeatureList();
            }
        }
    }

    public static boolean exists(Feature feature) {
        return feature != null && !feature.getName().isEmpty();
    }
}
