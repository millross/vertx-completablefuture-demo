package com.millross.test.java.future;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class AlreadyCompletedFutureTest {


    // Test which demonstrates that a completable future which has already been completed will immediately trigger
    // subsequent coputations applied to it. The significance of this is that we can make functions return
    // CompletableFutures of what they would already return rather than accept and complete CompletableFutures passed
    // as parameters
    @Test
    public void testAlreadyCompletedFutureTriggersSubsequentComputation() {
        final CompletableFuture<Integer> intFuture = CompletableFuture.completedFuture(1);
        intFuture
                .thenApply(i -> i + 4)
                .thenAccept(i -> System.out.println(i));
    }

}
