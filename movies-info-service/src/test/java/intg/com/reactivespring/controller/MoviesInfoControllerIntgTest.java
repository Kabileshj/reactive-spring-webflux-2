package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repo.MovieInfoRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

    @Autowired
    MovieInfoRepo movieInfoRepo;

    @Autowired
    WebTestClient webTestClient;

    static String movieInfoURL = "/v1/moviesInfo";

    @BeforeEach
    void setUp() {
        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        movieInfoRepo.saveAll(movieinfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepo.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        var movieInfo = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(movieInfoURL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                });
    }

    @Test
    void getAllMoviesInfo() {
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
    void getAllMoviesInfoWithYear() {
        var uri = UriComponentsBuilder.fromUriString("/v1/moviesInfo")
                .queryParam("year", 2005)
                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getMovieInfoById() {
        var movieInfoId = "def";

        webTestClient
                .get()
                .uri("/v1/moviesInfo/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNotFound();
//                .is2xxSuccessful()
//                .expectBody()
//                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
                /*.expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfo != null;
                });*/
    }

    @Test
    void updateMovieInfo() {
        var movieInfo = new MovieInfo(null, "Dark Knight Rises",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        var movieInfoId = "abc";
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

        webTestClient
                .delete()
                .uri("/v1/moviesInfo/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void findByYear() {

        var movieYear = 2005;
        var movieInfo = movieInfoRepo.findByYear(movieYear).log();

        StepVerifier.create(movieInfo)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void addMovieInfo1() {
        var movieInfo = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(movieInfoURL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                });

        var movieStreamFlux = webTestClient
                .get()
                .uri(movieInfoURL+"/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();
        StepVerifier.create(movieStreamFlux)
                .assertNext(movieInfo1 -> {
                    assert movieInfo1.getMovieInfoId()!=null;
                })
                .thenCancel()
                .verify();
    }
}