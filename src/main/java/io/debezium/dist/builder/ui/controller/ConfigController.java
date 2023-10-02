package io.debezium.dist.builder.ui.controller;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.dist.builder.ui.metadata.MetadataModelObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class ConfigController {

    @GetMapping("/")
    String getMainPage(ModelMap modelMap) throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("test.json");
        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        MetadataModelObject jsonObj = mapper.readValue(json, MetadataModelObject.class);

        modelMap.put("serverMetadata", jsonObj);
        return "main";
    }

}
