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

@Component
public class InitListener {
    Path metadataFile;
    private final String serverRepo = "https://github.com/debezium/debezium-server.git";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${debezium.server.repository.path}")
    private String repositoryPath;

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
