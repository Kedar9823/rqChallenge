package com.reliaquest.api.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Value("${emp.service.url}")
    private String baseUrl;

    @Bean
    public WebClient webClient() {

        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2));

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .build();

        return webClient;
    }
}
