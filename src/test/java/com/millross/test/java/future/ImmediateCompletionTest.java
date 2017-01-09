package com.millross.test.java.future;

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
 *
 */
@RunWith(VertxUnitRunner.class)
public class ImmediateCompletionTest {
    @Rule
    public final RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void testImmediateCompletion(TestContext context) {

        final Async async = context.async();
        final Vertx vertx = rule.vertx();
        final CompletableFuture<Integer> toComplete = new CompletableFuture<>();
        // delay future completion by 500 ms
        final String threadName = Thread.currentThread().getName();
        toComplete.complete(100);
        toComplete.thenRun(() -> {
            assertThat(Thread.currentThread().getName(), is(threadName));
            async.complete();
        });
    }

}
