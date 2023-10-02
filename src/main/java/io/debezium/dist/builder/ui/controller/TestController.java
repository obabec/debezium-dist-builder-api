package io.debezium.dist.builder.ui.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.dist.builder.ui.metadata.MetadataModelObject;
import io.debezium.server.dist.builder.DebeziumServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
public class TestController {

    @GetMapping("/test")
    DebeziumServer getServerJson() throws IOException {



        return new DebeziumServer();
    }

}
