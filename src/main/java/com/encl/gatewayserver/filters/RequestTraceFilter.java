package com.encl.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1)
@Component
public class RequestTraceFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestTraceFilter.class);

    @Autowired
    FilterUtility filterUtility;


    /**
     * Mono is a Reactive Streams Publisher, and is used to represent a single value or an empty value that will be available in the future.
     *
     * It is part of the Project Reactor library, which is a reactive programming library for building non-blocking applications on the JVM.
     * In the context of Spring WebFlux, Mono is often used to represent asynchronous operations that may complete with a single result or no result at all.
     *
     * In this method, the filter checks if a correlation ID is present in the request headers. If it is present, it logs the correlation ID.
     * If it is not present, it generates a new correlation ID, adds it to the request headers, and logs the generated correlation ID.
     * Finally, it calls the next filter in the chain by invoking chain.filter(exchange) and returns the resulting Mono<Void>.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();

        /** Check if the correlation ID is present in the request headers.
         * If it is present, log the correlation ID.
         * If it is not present, generate a new correlation ID, add it to the request headers, and log the generated correlation ID.
         */
        if (isCorrelationIdPresent(requestHeaders)) {
            logger.debug("bankapp-correlation-id found in RequestTraceFilter : {}",
                    filterUtility.getCorrelationId(requestHeaders));
        } else {
            String correlationID = generateCorrelationId();
            exchange = filterUtility.setCorrelationId(exchange, correlationID);
            logger.debug("bankapp-correlation-id generated in RequestTraceFilter : {}", correlationID);
        }
        //ALl filtrs are executed in order of their precedence. This methid must call the next filter in the chain
        return chain.filter(exchange);
    }

    /** Check if the correlation ID is present in the request headers.
     * If it is present, return true. Otherwise, return false.
     */
    private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) {
        if (filterUtility.getCorrelationId(requestHeaders) != null) {
            return true;
        } else {
            return false;
        }
    }

    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }

}
