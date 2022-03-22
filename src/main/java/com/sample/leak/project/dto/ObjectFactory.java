package com.sample.leak.project.dto;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public ObjectFactory() {
    }

    public DataDTO createDataDTO() {
        return new DataDTO();
    }
}
