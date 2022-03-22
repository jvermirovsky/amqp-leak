package com.sample.leak.project.dto;


import com.sun.istack.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"test"}
)
@XmlRootElement(
        name = "DataDTO",
        namespace = "http://test.project.net/DataDto"
)
public class DataDTO {

    @XmlElement(
            namespace = "http://test.project.net/DataDto",
            required = true
    )
    @NotNull
    private String test;

    public DataDTO() {

    }

    public DataDTO(String test) {
        this.test = test;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
