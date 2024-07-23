package com.example.sseclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
public class SseClientRunner implements CommandLineRunner {

    private final WebClient webClient;

    @Value("${sse.stream.url}")
    private String url;

    public SseClientRunner() {
        this.webClient = WebClient.create(url);
    }

    @Override
    public void run(String... args) {
        consumeServerSentEvents();
    }

    public void consumeServerSentEvents() {
        Flux<ServerSentEvent> eventStream = webClient.get()
//                .uri("/")
                .retrieve()
//                .bodyToFlux(String.class)
                .bodyToFlux(ServerSentEvent.class)
                .doOnError(WebClientResponseException.class, ex -> {
                    System.out.println("Error receiving SSE: " + ex.getMessage());
                });

        eventStream.subscribeOn(Schedulers.boundedElastic())
                .subscribe(event -> {
                    System.out.println("Received event: " + event);
                });
    }
}

