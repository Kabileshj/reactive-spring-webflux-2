package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repo.ReviewReactiveRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private ReviewReactiveRepo reviewReactiveRepo;

    @Autowired
    private Validator validator;

    Sinks.Many<Review> reviewSinks = Sinks.many().replay().all();

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepo::save)
                .doOnNext(review
                        -> reviewSinks.tryEmitNext(review))
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("constraintViolations : {}", constraintViolations);
        if (constraintViolations.size() > 0) {
            var errmsg = constraintViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errmsg);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var movieInfoId = request.queryParam("movieInfoId");
        if (movieInfoId.isPresent()) {
            var reviews = reviewReactiveRepo.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return ServerResponse.status(HttpStatus.OK).body(reviews, Review.class);
        } else {
            var reviews = reviewReactiveRepo.findAll();
            return ServerResponse.status(HttpStatus.OK).body(reviews, Review.class);
        }
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepo.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for id " + reviewId)));

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepo::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepo.findById(reviewId);

        return existingReview.flatMap(review -> reviewReactiveRepo.deleteById(reviewId))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepo.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for id " + reviewId)));
        return ServerResponse.status(HttpStatus.OK).body(existingReview, Review.class);
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewSinks.asFlux(),Review.class)
                .log();
    }
}
