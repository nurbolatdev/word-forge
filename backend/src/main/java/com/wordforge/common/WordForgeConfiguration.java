package com.wordforge.common;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ExternalServiceProperties.class)
public class WordForgeConfiguration {
}
