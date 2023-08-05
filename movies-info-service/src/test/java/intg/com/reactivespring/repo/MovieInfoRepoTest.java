package com.reactivespring.repo;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepoTest {
    @Autowired
    MovieInfoRepo movieInfoRepo;

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
    void findall() {
        var movieInfoFlux = movieInfoRepo.findAll().log();

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        var movieInfoMono = movieInfoRepo.findById("abc").log();

        StepVerifier.create(movieInfoMono)
                //.expectNextCount(3)
                .assertNext(movieInfo -> assertEquals("Dark Knight Rises", movieInfo.getName()))
                .verifyComplete();
    }

    @Test
    void save() {
        var movieInfo = new MovieInfo(null, "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var movieInfoMono = movieInfoRepo.save(movieInfo).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals("Batman Begins1", movieInfo1.getName());
                })
                .verifyComplete();
    }

    @Test
    void update() {
        var movieInfo = movieInfoRepo.findById("abc").block();
        movieInfo.setYear(2023);

        var movieInfoMono = movieInfoRepo.save(movieInfo).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo1 -> {
                    assertEquals(2023, movieInfo1.getYear());
                })
                .verifyComplete();
    }

    @Test
    void delete() {
        movieInfoRepo.deleteById("abc").block();

        var movieInfoFlux = movieInfoRepo.findAll().log();

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }
}