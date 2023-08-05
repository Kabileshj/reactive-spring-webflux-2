package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 8084)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestPropertySource(
        properties = {
                "restClient.moviesInfourl=http://localhost:8084/v1/moviesInfo",
                "restClient.reviewsurl=http://localhost:8084/v1/review"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void retrieveMovieInfo() {
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/moviesInfo/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo service not available")
                ));

        stubFor(get(urlPathEqualTo("/v1/review"))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBodyFile("reviews.json")));

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class);
//                .isOk()
//                .expectBody(Movie.class)
//                .consumeWith(movieEntityExchangeResult -> {
//                    var movie = movieEntityExchangeResult.getResponseBody();
//                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
//                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
//                });
        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/moviesInfo/" + movieId)));
    }

    @Test
    void retrieveMovieInfo_404() {
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/moviesInfo/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/review"))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 0;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });
    }
}
