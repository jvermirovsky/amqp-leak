package com.sample.leak.project.endpoint;

import com.sample.leak.project.configuration.RabbitConfiguration;
import com.sample.leak.project.dto.DataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test-api")
public class RestEndpoint {

    @Autowired
    private RabbitConfiguration.IOutgoingGateway outgoingGateway;

    @GetMapping(value = "/test/{param}")
    public void addToQueue(@PathVariable String param) {

        outgoingGateway.sendInfoNotification(new DataDTO(param));
    }
}
