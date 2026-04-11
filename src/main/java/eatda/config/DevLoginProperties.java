package eatda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "local-docker")
public record DevLoginProperties(boolean enabled) {
}
