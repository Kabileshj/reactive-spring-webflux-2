package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    @Test
    void addMovieInfo() {
        var movieInfo = new MovieInfo(null, "Batman Begins",
                2005, List.of(""), LocalDate.parse("2005-06-15"));

//        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(
//                Mono.just(new MovieInfo("MockId", "Batman Begins",
//                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        webTestClient
                .post()
                .uri("/v1/moviesInfo")
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(EntityExchangeResult -> {
                    var responseBody = EntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                });
//                .expectBody(MovieInfo.class)
//                .consumeWith(movieInfoEntityExchangeResult -> {
//                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
//                    assert savedMovieInfo != null;
//                    assert savedMovieInfo.getMovieInfoId() != null;
//                    assertEquals("MockId", savedMovieInfo.getMovieInfoId());
//                });
    }

    @Test
    void getAllMoviesInfo() {
        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(moviesInfoServiceMock.getAllMoviesInfo()).thenReturn(Flux.fromIterable(movieinfos));

        webTestClient
                .get()
                .uri("/v1/moviesInfo")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        var movieInfoId = "abc";

        when(moviesInfoServiceMock.getMovieInfoById(movieInfoId)).thenReturn(
                Mono.just(new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))));

        webTestClient
                .get()
                .uri("/v1/moviesInfo/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void updateMovieInfo() {
        var movieInfo = new MovieInfo(null, "Batman Begins",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
        var movieInfoId = "abc";

        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(
                Mono.just(new MovieInfo(movieInfoId, "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))));

        webTestClient
                .put()
                .uri("/v1/moviesInfo/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert updatedMovieInfo != null;
                    assert updatedMovieInfo.getMovieInfoId() != null;
                    assertEquals("Dark Knight Rises", updatedMovieInfo.getName());
                });
    }

    @Test
    void deleteMovieInfoById() {
        var movieInfoId = "abc";

        when(moviesInfoServiceMock.deleteMovieInfoById(isA(String.class))).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/v1/moviesInfo/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}