package eatda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dev-login")
public record DevLoginProperties(boolean enabled) {
}
