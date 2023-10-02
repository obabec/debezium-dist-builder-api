package io.debezium.dist.builder.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.dist.builder.ui.metadata.MetadataModelObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class DistBuilderUiApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(DistBuilderUiApplication.class, args);
    }

}
