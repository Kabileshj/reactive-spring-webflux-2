package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewRoute(ReviewHandler reviewHandler) {
        return route()
                .nest(path("/v1/review"), builder -> {
                    builder.POST("", request -> reviewHandler.addReview(request))
                            .DELETE("/{id}", request -> reviewHandler.deleteReview(request))
                            .PUT("/{id}", request -> reviewHandler.updateReview(request))
                            .GET("/{id}", request -> reviewHandler.getReview(request))
                            .GET("", request -> reviewHandler.getReviews(request))
                            .GET("/stream", request -> reviewHandler.getReviewsStream(request));
                })
                .GET("/v1/helloworld", (request -> ServerResponse.ok().bodyValue("hello world")))
                .build();
    }
}
