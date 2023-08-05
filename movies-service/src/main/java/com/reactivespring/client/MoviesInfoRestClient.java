package com.reactivespring.client;

import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class MoviesInfoRestClient {
    private WebClient webClient;

    @Value("${restClient.moviesInfourl}")
    private String movieInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        var retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(ex -> ex instanceof MoviesInfoServerException)
                .onRetryExhaustedThrow((retrieveBackOffSpec,retrySignal) ->
                        Exceptions.propagate(retrySignal.failure()));

        var url = movieInfoUrl.concat("/{id}");
        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException(
                                "No MovieInfo found for the given id " + movieId,
                                clientResponse.statusCode().value()));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMsg -> Mono.error(new MoviesInfoClientException(
                                    responseMsg, clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMsg -> Mono.error(new MoviesInfoServerException(
                                    responseMsg)));
                })
                .bodyToMono(MovieInfo.class)
                //.retry(3)
                .retryWhen(retrySpec)
                .log();
    }

    public Flux<MovieInfo> retrieveMovieInfoStream() {
        var url = movieInfoUrl.concat("/stream");

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMsg -> Mono.error(new MoviesInfoClientException(
                                    responseMsg, clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("Status code is : {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMsg -> Mono.error(new MoviesInfoServerException(
                                    responseMsg)));
                })
                .bodyToFlux(MovieInfo.class)
                .log();
    }
}
