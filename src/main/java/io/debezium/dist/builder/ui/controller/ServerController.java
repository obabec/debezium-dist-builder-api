/*
 * MIT License
 *
 * Copyright (c) [2024] [Ondrej Babec <ond.babec@gmail.com>]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.debezium.dist.builder.ui.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.server.dist.builder.CustomDebeziumServer;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Objects.nonNull;

/**
 *
 */
@Controller
@CrossOrigin(origins = "*")
public class ServerController {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${debezium.server.repository.root.path}")
    private String repositoryPath;

    /**
     * Generate distribution endpoint.
     * @param distribution Serialized distribution string.
     * @param truststore Truststore located as part of the mulitpart file.
     * @param keystore Keystore located as part of the mulitpart file.
     * @return Zip with complete distribution.
     */
    @RequestMapping(
            value = "/generateDistribution",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Object> getMainPage(@RequestParam("distribution") String distribution, @RequestParam(value = "truststore", required = false) MultipartFile truststore, @RequestParam(value = "keystore", required = false) MultipartFile keystore) {
        ObjectMapper objectMapper = DeserializationUtils.getDefaultMapper();
        storeMultipartFile(keystore);
        storeMultipartFile(truststore);
        ByteArrayResource resource;
        try {
            CustomDebeziumServer server = objectMapper.readValue(distribution, CustomDebeziumServer.class);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            new DebeziumServerDistributionBuilder()
                    .withDebeziumServer(server)
                    .withLocalProject(repositoryPath)
                    .build()
                    .generateConfigurationProperties()
                    .zipDistribution(repositoryPath, bos);

            resource = new ByteArrayResource(bos.toByteArray());
        } catch (IOException e) {
            LOGGER.error("Error occurred during generateDistribution POST: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     *
     * @return
     * @throws IOException
     */
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
        if (nonNull(file)) {
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
}
