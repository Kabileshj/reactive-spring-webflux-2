package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repo.MovieInfoRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MoviesInfoService {

    private MovieInfoRepo movieInfoRepo;

    public MoviesInfoService(MovieInfoRepo movieInfoRepo) {
        this.movieInfoRepo = movieInfoRepo;
    }

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return movieInfoRepo.save(movieInfo);
    }

    public Flux<MovieInfo> getAllMoviesInfo() {
        return movieInfoRepo.findAll();
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {
        return movieInfoRepo.findById(id);
    }

    public Flux<MovieInfo> getMovieInfoByYear(Integer year) {
        return movieInfoRepo.findByYear(year);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo movieInfo, String id) {
        return movieInfoRepo.findById(id)
                .flatMap(movieInfoNew -> {
                    movieInfoNew.setCast(movieInfo.getCast());
                    movieInfoNew.setMovieInfoId(movieInfo.getMovieInfoId());
                    movieInfoNew.setYear(movieInfo.getYear());
                    movieInfoNew.setReleaseDate(movieInfo.getReleaseDate());
                    return movieInfoRepo.save(movieInfoNew);
                });
    }

    public Mono<Void> deleteMovieInfoById(String id) {
        return movieInfoRepo.deleteById(id);
    }
}
