package com.poc.subscriber;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PocEventSubscriber extends AbstractSubscriber implements EventSubscriber {
	
	private static final Logger log = LoggerFactory.getLogger(PocEventSubscriber.class);

	public PocEventSubscriber(
			@Value("${gcp.project.id}") String projectId,
			@Value("${gcp.subscription.id.poc}") String subscriptionId) {
		super(projectId, subscriptionId);
	}

	@Override
	public MessageReceiver getMessageReceiver() {
		return (PubsubMessage message, AckReplyConsumer consumer) -> {
			long inicialTime = System.currentTimeMillis();
			try {
				String data = message.getData().toStringUtf8();
				long latency = NumberUtils.toLong(message.getAttributesMap().get("latency"));
				Thread.sleep(latency);
				log.info("{} ms | TEST FROM PUB/SUB: {}={}",  (System.currentTimeMillis() - inicialTime), message.getOrderingKey(), data);
			} catch (Exception e) {
				log.error("Generic Unhandled: {} ms | {} | {} ", (System.currentTimeMillis() - inicialTime), e.getMessage(), message, e);
			}
			consumer.ack();
		};
	}

	@Override
	public int getNumberOfThreads() {
		return 5;
	}
}
