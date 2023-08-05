package com.reactivespring.repo;

import com.reactivespring.domain.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MovieInfoRepo extends ReactiveMongoRepository<MovieInfo, String> {

    Flux<MovieInfo> findByYear(Integer year);
}
