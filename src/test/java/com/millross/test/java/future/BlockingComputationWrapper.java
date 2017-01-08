package com.millross.test.java.future;

import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 *
 */
public class BlockingComputationWrapper {

    private final Vertx vertx;

    public BlockingComputationWrapper(final Vertx vertx) {
        this.vertx = vertx;
    }

    public <T> CompletableFuture<T> fromBlockingComputation(Supplier<T> computation) {

        final CompletableFuture<T> completableFuture = new CompletableFuture<T>();

        try {
            vertx.<T>executeBlocking(future -> future.complete(computation.get()),
                    false,
                    result -> {
                        if (result.succeeded()) {
                            completableFuture.complete(result.result());
                        } else {
                            completableFuture.completeExceptionally(result.cause());
                        }
                    });
        } catch (Throwable t) {
            completableFuture.completeExceptionally(t);
        }

        return completableFuture;
    }

}
