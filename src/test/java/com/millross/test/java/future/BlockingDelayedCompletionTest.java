package com.millross.test.java.future;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for demonstrating correct and incorrect approaches to wrapping blocking code for an async
 * framework, assuming that the async framework requires any callback to be performed on a specific thread
 */
@RunWith(VertxUnitRunner.class)
public class BlockingDelayedCompletionTest {

    @Rule
    public final RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void testBlockingDelayedCompletionCorrectlyWrapped(TestContext context) {

        final Async async = context.async();
        final Vertx vertx = rule.vertx();
        final CompletableFuture<Integer> toComplete = new CompletableFuture<>();
        // delay future completion by 500 ms
        final String threadName = Thread.currentThread().getName();

        vertx.executeBlocking((Future<Integer> future) -> {
            try {
                Thread.sleep(1000);
                future.complete(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                context.fail("Sleep failed");
            }
        }, false, result -> toComplete.complete(result.result()));

        toComplete.thenRun(() -> {
            assertThat(Thread.currentThread().getName(), is(threadName));
            async.complete();
        });
    }

}
