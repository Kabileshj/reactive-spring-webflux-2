package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repo.ReviewReactiveRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepo reviewReactiveRepo;

    @BeforeEach
    void setUp() {
        var reviewsList = List.of(
                new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactiveRepo.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepo.deleteAll().block();
    }

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        webTestClient
                .post()
                .uri("/v1/review")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });
    }

    @Test
    void getReviews() {
        var uri = UriComponentsBuilder.fromUriString("/v1/review")
                .queryParam("movieInfoId", 1)
                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void updateReview() {
        var review = new Review(null, 1L, "Awesome Movie", 8.0);
        var reviewId = "def";

        webTestClient
                .put()
                .uri("/v1/review/{id}", reviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var updatedReview = reviewEntityExchangeResult.getResponseBody();
                    assert updatedReview != null;
                    assert updatedReview.getReviewId() != null;
                    assertEquals(8.0, updatedReview.getRating());
                });
    }

    @Test
    void getReviewById() {
        var reviewId = "abc";

        webTestClient
                .get()
                .uri("/v1/review/{id}", reviewId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.rating").isEqualTo(9.0);
    }

    @Test
    void deleteReviewById() {
        var reviewId = "abc";

        webTestClient
                .delete()
                .uri("/v1/review/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
