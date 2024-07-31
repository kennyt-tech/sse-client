package com.example.sseclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class SseClientRunner implements CommandLineRunner {

    private final WebClient webClient;

    public SseClientRunner(@Value("${sse.stream.url}") String url) {
        this.webClient = WebClient.create(url);
    }

    @Override
    public void run(String... args) {
        consumeServerSentEvents();
    }

    public void consumeServerSentEvents() {
        Flux<ServerSentEvent> eventStream = webClient.get()
                .retrieve()
                .bodyToFlux(ServerSentEvent.class)
                .doOnError(WebClientResponseException.class, ex -> log.error("Error receiving SSE: " + ex.getMessage()));

        eventStream.subscribeOn(Schedulers.boundedElastic())
                .subscribe(event -> {
                    log.info("Received event: " + event.event());
                    log.info("Received data: " + event.data());
                });
    }
}

