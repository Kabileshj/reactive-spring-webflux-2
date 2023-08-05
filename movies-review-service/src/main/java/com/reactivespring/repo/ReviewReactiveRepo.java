package com.reactivespring.repo;

import com.reactivespring.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReviewReactiveRepo extends ReactiveMongoRepository<Review, String> {
    Flux<Review> findReviewsByMovieInfoId(Long movieInfoId);
}
