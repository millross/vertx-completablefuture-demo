package com.millross.test.java.future;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for demonstrating correct and incorrect approaches to wrapping blocking code for an async
 * framework, assuming that the async framework requires any callback to be performed on a specific thread
 */
@RunWith(VertxUnitRunner.class)
public class BlockingDelayedCompletionTest {

    @Rule
    public final RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void setupExceptionHandler(TestContext context) {
        rule.vertx().exceptionHandler(context.exceptionHandler());
    }

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

    /**
     * Test to show that if we wrap the blocking code incorrectly (by completing the future
     * within the blocking code rather than in the async result handler for it) then our
     * future completion handler will not occur on the desired thread, breaking any threading model
     * in place.This is significant for, for example, vert.x, where each verticle instance
     * is bound to a specific thread and future completion must happen on that thread if we want to ensure that
     * all relevant objects will be treated safely. Very significant for http handling where the relevant classes
     * are not expected to be thread-safe because they are expected to be bound to a single verticle's event loop.s
     * @param context
     */
    @Test
    public void testBlockingDelayedCompletionIncorrectlyWrapped(TestContext context) {

        final Async async = context.async();
        final Vertx vertx = rule.vertx();
        final CompletableFuture<Integer> toComplete = new CompletableFuture<>();
        // delay future completion by 500 ms
        final String threadName = Thread.currentThread().getName();

        vertx.executeBlocking((Future<Void> future) -> {
            try {
                Thread.sleep(1000);
                toComplete.complete(1000);
                future.complete(null);
            } catch (InterruptedException e) {
                e.printStackTrace();
                context.fail("Sleep failed");
            }
        }, false, result -> {});

        toComplete.thenRun(() -> {
            assertThat(Thread.currentThread().getName(), is(not(threadName)));
            async.complete();
        });
    }

    @Test
    public void testBlockingDelayedUsingWrapper(TestContext context) {
        final Async async = context.async();
        final Vertx vertx = rule.vertx();

        // delay future completion by 500 ms
        final String threadName = Thread.currentThread().getName();

        final BlockingComputationWrapper wrapper = new BlockingComputationWrapper(vertx);
        final CompletableFuture<Integer> toComplete = wrapper.fromBlockingComputation(() -> {
            try {
                Thread.sleep(1000);
                return 500;
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        });

        toComplete.thenRun(() -> {
            assertThat(Thread.currentThread().getName(), is(threadName));
            async.complete();
        });
    }

}
