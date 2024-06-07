package com.poc.publisher;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import java.time.Instant;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PocPublisher {

    private final Logger log = LoggerFactory.getLogger(PocPublisher.class);

    private final Publisher pocPublisher;

    private final ObjectMapper mapper;

    public PocPublisher(String projectId, String positionTopicId) {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        try {
            ProjectTopicName topic = ProjectTopicName.of(projectId, positionTopicId);
            this.pocPublisher = Publisher.newBuilder(topic)
                    .setEnableMessageOrdering(true)
                    .build();
            log.info("PUBLISHER INITIALIZED AT TOPIC:  {} ", positionTopicId);
        } catch (IOException e) {
            log.info("PUBLISHER_ERROR_INITIALIZATION {}", positionTopicId, e);
            throw new RuntimeException(e);
        }
    }

    public void publishPoc(Integer latencyMs, Integer orderKey, int count) {
        try {
            PubsubMessage pubsubMessage = buildDeviceMessage(latencyMs.toString(), orderKey.toString(), count);
            ApiFuture<String> future = pocPublisher.publish(pubsubMessage);
            log.info("MessageID {}", future.get());
        } catch (Exception e) {
            log.error("Error publishing position ", e);
        }
    }

    private PubsubMessage buildDeviceMessage(String latencyMs, String orderKey, int count) throws JsonProcessingException {
        byte[] writeValueAsBytes = mapper.writeValueAsBytes(new PocData(count));
        return PubsubMessage.newBuilder()
                .putAttributes("latency", latencyMs)
                .setData(ByteString.copyFrom(writeValueAsBytes))
                .setOrderingKey(orderKey)
                .build();
    }

    static class PocData {
        public final Integer order;
        public final String value = "sample";
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        public final Instant time = Instant.now();
        PocData(Integer order) {
            this.order = order;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        PocPublisher pocPub = new PocPublisher("selsyn-test-us", "poc-test-topic");

        IntStream.range(0, 10000)
                .parallel()
                .forEach(i -> {
                    int orderKey =  i % 10;
                    pocPub.publishPoc(300, orderKey, i);
                });

        Thread.sleep(500L);
    }
}
