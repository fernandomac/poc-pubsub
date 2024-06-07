package com.poc.web.rest.endpoint;

import com.poc.application.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
		webEnvironment = NONE,
		classes = { AppConfig.class }
)
public class ContextLoadTest {
	
	 @Test
	 void loadContext() {

	 }
}
