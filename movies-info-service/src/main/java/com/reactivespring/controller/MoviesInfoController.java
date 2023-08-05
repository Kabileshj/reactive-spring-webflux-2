package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@Slf4j
public class MoviesInfoController {
    private MoviesInfoService moviesInfoService;

    Sinks.Many<MovieInfo> movieInfoSinks = Sinks.many().replay().all();

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }

    @PostMapping("/moviesInfo")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return moviesInfoService.addMovieInfo(movieInfo).log()
                .doOnNext(savedInfo -> movieInfoSinks.tryEmitNext(savedInfo));
    }

    @GetMapping("/moviesInfo")
    public Flux<MovieInfo> getAllMoviesInfo(@RequestParam(name = "year", required = false) Integer year) {
        log.info("year is : {}", year);
        if (year != null) {
            return moviesInfoService.getMovieInfoByYear(year).log();
        }
        return moviesInfoService.getAllMoviesInfo().log();
    }

    @GetMapping("/moviesInfo/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {
        return moviesInfoService.getMovieInfoById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @GetMapping(value = "/moviesInfo/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> getMovieInfoById() {
        return movieInfoSinks.asFlux();
    }

    @PutMapping("/moviesInfo/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo movieInfo, @PathVariable String id) {
        return moviesInfoService.updateMovieInfo(movieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/moviesInfo/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfoById(@PathVariable String id) {
        return moviesInfoService.deleteMovieInfoById(id).log();
    }
}
