package com.example.demo.jsonData;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//@JacksonXmlRootElement(localName = "WhiteMaster")
@JsonPropertyOrder({"id", "Name","emptyString","details"})
@JsonIgnoreProperties({"ignoredField","ignoredField2","ignoredField3",})
@Data
public class Master {
    @JsonProperty("ID")
    String id;
    @JsonProperty("Name")
    String Name;
    String emptyString;
//    @JsonIgnore
    String ignoredField;
    String ignoredField2;
    String ignoredField3;
    String ignoredField4;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "dd-MM-yyyy hh:mm:ss")
    Date myDate;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Goods")
    List<Detail> details = new ArrayList<>();
}
