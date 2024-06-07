package com.poc.subscriber;

import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

public abstract class AbstractSubscriber implements EventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(AbstractSubscriber.class);
    private Subscriber subscriber;
    private final ProjectSubscriptionName subscriptionName;

    protected AbstractSubscriber(String projectId, String subscriptionId) {
        this.subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
    }

    public abstract MessageReceiver getMessageReceiver();

    public abstract int getNumberOfThreads();

    @Override
    public void subscribeAsync() {
        ExecutorProvider executorProvider =
                InstantiatingExecutorProvider.newBuilder()
                        .setExecutorThreadCount(getNumberOfThreads()).build();

        FlowControlSettings flowContrlSettings = FlowControlSettings.newBuilder()
                .setMaxOutstandingElementCount(40L)
                .build();

        subscriber = Subscriber.newBuilder(subscriptionName, getMessageReceiver())
                .setExecutorProvider(executorProvider)
                .setMaxAckExtensionPeriod(Duration.ofMinutes(0L))
                .setMaxDurationPerAckExtension(Duration.ofMinutes(0L))
                .setFlowControlSettings(flowContrlSettings)
                .setParallelPullCount(5)
                .build();

        subscriber.startAsync().awaitRunning();

        log.info("{} threads listening for messages on {}", getNumberOfThreads(), subscriptionName);
    }

    @Override
    public void stopAsync() {
        subscriber.stopAsync();
        try {
            subscriber.awaitTerminated(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Timeout awaiting terminate {} | {}", subscriptionName.toString(), e.getMessage(), e);
        }
    }
}
