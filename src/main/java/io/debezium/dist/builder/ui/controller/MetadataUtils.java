package io.debezium.dist.builder.ui.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class MetadataUtils {
    public static Path getMetadataFilePath() {
        Path source = Paths.get(Objects.requireNonNull(MetadataUtils.class.getResource("/")).getPath());
        return Paths.get(source.toAbsolutePath() + "/metadata.json");
    }
}
