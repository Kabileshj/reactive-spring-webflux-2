package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repo.ReviewReactiveRepo;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactiveRepo reviewReactiveRepo;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void addReview() {
        var review = new Review(null, null, "Awesome Movie", -9.0);

        when(reviewReactiveRepo.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        webTestClient
                .post()
                .uri("/v1/review")
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
