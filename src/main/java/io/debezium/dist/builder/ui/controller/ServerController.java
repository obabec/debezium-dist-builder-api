package io.debezium.dist.builder.ui.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.server.dist.builder.DebeziumServer;
import io.debezium.server.dist.builder.DebeziumServerDistributionBuilder;
import io.debezium.server.dist.builder.utils.DeserializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

@Controller
@CrossOrigin(origins = "*")
public class ServerController {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${debezium.server.repository.root.path}")
    private String repositoryPath;

    @RequestMapping(
            value = "/generateDistribution",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Object> getMainPage(@RequestParam("distribution") String distribution, @RequestParam("truststore") MultipartFile truststore, @RequestParam("keystore") MultipartFile keystore) {
        ObjectMapper objectMapper = DeserializationUtils.getDefaultMapper();
        storeMultipartFile(keystore);
        storeMultipartFile(truststore);
        ByteArrayResource resource;
        try {
            DebeziumServer server = objectMapper.readValue(distribution, DebeziumServer.class);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            new DebeziumServerDistributionBuilder()
                    .withDebeziumServer(server)
                    .withVersion("2.3.2.Final")
                    .withLocalProject(repositoryPath)
                    .build()
                    .generateConfigurationProperties()
                    .zipDistribution(repositoryPath, bos);

            resource = new ByteArrayResource(bos.toByteArray());
            int x = 5;
        } catch (IOException e) {
            LOGGER.error("Error occured: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/metadata",
            produces="application/json",
            method = RequestMethod.GET
    )
    ResponseEntity<Object> getMetadata() throws IOException {
        String metadata = Files.readString(MetadataUtils.getMetadataFilePath());
        return new ResponseEntity<>(metadata, HttpStatus.OK);
    }

    private void storeMultipartFile(MultipartFile file) {
        File f = new File(repositoryPath + file.getOriginalFilename());
        if (f.exists()) {
            f.delete();
        }
        try (OutputStream os = new FileOutputStream(f)) {
            os.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
