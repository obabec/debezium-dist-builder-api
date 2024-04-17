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

import io.debezium.server.dist.builder.DebeziumServerDistributionBuilder;
import io.debezium.server.dist.builder.metadata.MetadataGenerator;
import jakarta.annotation.PostConstruct;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This listener is used to perform all pre-run steps. Including metadata generation and cloning the repository.
 */
@Component
public class InitListener {
    Path metadataFile;
    private final String serverRepo = "https://github.com/debezium/debezium-server.git";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${debezium.server.repository.path}")
    private String repositoryPath;

    /**
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */

    /**
     * Generates metadata and clones the repository right after the Initial construction completes.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @PostConstruct
    public void generateMetadata() throws IOException, ClassNotFoundException {

        logger.info("Generating metadata");
        metadataFile = MetadataUtils.getMetadataFilePath();

        MetadataGenerator metadataGenerator = new MetadataGenerator();
        metadataGenerator.generateMetadata(new FileOutputStream(metadataFile.toAbsolutePath().toString()));
        cloneServer();
    }

    private void deleteCurrentRepo() throws IOException {
        File f = new File(repositoryPath);

        if (f.exists() && f.isDirectory()) {
            try (Stream<Path> pathStream = Files.walk(Paths.get(repositoryPath))) {
                pathStream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    private void cloneServer() throws IOException {
        deleteCurrentRepo();

        try {
            Git distRepo;
            distRepo = Git.cloneRepository().setDepth(1).setURI(serverRepo).setDirectory(new File(this.repositoryPath)).setNoCheckout(true).call();
            distRepo.checkout().setName("main").setStartPoint("origin/main").addPath("debezium-server-dist").call();
            distRepo.getRepository().close();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
