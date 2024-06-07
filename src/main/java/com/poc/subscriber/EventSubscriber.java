package com.poc.subscriber;

public interface EventSubscriber {

	void subscribeAsync();

	void stopAsync();
}