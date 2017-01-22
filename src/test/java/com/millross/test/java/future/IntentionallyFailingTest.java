package com.millross.test.java.future;

import io.vertx.core.Context;
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
 * This test is to show the impact of Context::runOnContext on threading and a particular failure scenario when
 * processing a completable future, to ask the vert.x guys why runOnContext is needed
 */
@RunWith(VertxUnitRunner.class)
public class IntentionallyFailingTest {

    @Rule
    public final RunTestOnContext rule = new RunTestOnContext();

    @Test(timeout = 2000)
    public void testImmediateCompletionWithoutContext(TestContext context) {

        final Async async = context.async();
        final CompletableFuture<Integer> toComplete = new CompletableFuture<>();
        // delay future completion by 500 ms
        final String threadName = Thread.currentThread().getName();
        toComplete.complete(100);
        toComplete.thenAccept(t -> {
            assertThat(Thread.currentThread().getName(), is(threadName));
            System.out.println("Thread name check passed");
            assertThat(t, is(101));
            async.complete();
        });
    }

    @Test(timeout = 2000)
    public void testImmediateCompletionWithContext(TestContext context) {

        final Async async = context.async();
        final Context vertxContext = rule.vertx().getOrCreateContext();

        final CompletableFuture<Integer> toComplete = new CompletableFuture<>();
        // delay future completion by 500 ms
        final String threadName = Thread.currentThread().getName();
        toComplete.complete(100);
        toComplete.thenAccept(t -> {
            vertxContext.runOnContext(v -> {
                assertThat(Thread.currentThread().getName(), is(threadName));
                System.out.println("Thread name check passed");
                assertThat(t, is(101));
                async.complete();
            });
        });
    }

}
