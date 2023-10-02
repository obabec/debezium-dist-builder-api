package io.debezium.dist.builder.ui.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataModelObject {
    String name;
    String type;
    String clazz;
    List<MetadataModelObject> fields;
    List<MetadataModelObject> options;
    List<String> variants;

    Boolean selected;
    String value;


    public MetadataModelObject() {
    }
}
