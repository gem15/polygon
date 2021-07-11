package com.example.demo.jsonData;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@JacksonXmlRootElement(localName = "Goods")
@JsonPropertyOrder({"id", "name"})

@Data
public class Detail {
    String id;
    @JsonProperty("Article")
    String name;
}
