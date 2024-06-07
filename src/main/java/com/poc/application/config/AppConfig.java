package com.poc.application.config;


import com.poc.subscriber.PocEventSubscriber;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value={"com.poc.subscriber"})
public class AppConfig extends ResourceConfig {

    @Autowired
    private PocEventSubscriber pocEventSubscriber;

    @PostConstruct
    public void init() {
        pocEventSubscriber.subscribeAsync();
    }

    @PreDestroy
    public void quit() {
        pocEventSubscriber.stopAsync();
    }
}

