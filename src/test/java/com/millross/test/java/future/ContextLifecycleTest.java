package com.millross.test.java.future;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.BiConsumer;

/**
 *
 */
@RunWith(VertxUnitRunner.class)
public class ContextLifecycleTest {

    @Rule
    public final RunTestOnContext rule = new RunTestOnContext();

    @Before
    public final void applyExceptionHandling(final TestContext context) {
        rule.vertx().exceptionHandler(context.exceptionHandler());
    }


    @Test(timeout = 2000)
    public void doesContextPropagateToDeployedVerticle(final TestContext testContext) throws Exception {

        executeTest(testContext, (vertx, asyncResultHandler) -> {

            final Verticle deployingVerticle = new AbstractVerticle() {

                @Override
                public void start(Future<Void> startFuture) throws Exception {
                    final Context context = vertx.getOrCreateContext();
                    System.out.println("Starting deploying verticle from context" + context);
                    vertx.deployVerticle(new VerticleToDeploy(), result -> {
                        if (result.succeeded()) {
                            startFuture.complete();
                        } else {
                            startFuture.fail(result.cause());
                        }
                    });
                }
            };

            vertx.deployVerticle(deployingVerticle, asyncResultHandler);
        });

    }

    @Test(timeout = 2000)
    public void doesContextPropagateToMultipleDeployedVerticleInstances(final TestContext testContext) throws Exception {

        executeTest(testContext, (vertx, asyncResultHandler) -> {

            final Verticle deployingVerticle = new AbstractVerticle() {

                @Override
                public void start(Future<Void> startFuture) throws Exception {
                    final Context context = vertx.getOrCreateContext();
                    System.out.println("Starting deploying verticle from context" + context);
                    vertx.deployVerticle(VerticleToDeploy.class.getName(), new DeploymentOptions().setInstances(4), result -> {
                        if (result.succeeded()) {
                            startFuture.complete();
                        } else {
                            startFuture.fail(result.cause());
                        }
                    });
                }
            };

            vertx.deployVerticle(deployingVerticle, asyncResultHandler);
        });

    }

    @Test(timeout = 2000)
    public void testContextPropagationForMultipleInstances(final TestContext testContext) throws Exception {

        executeTest(testContext, (vertx, asyncResultHandler) -> {
            final Context context = vertx.getOrCreateContext();
            System.out.println("Starting deploying multiple verticles from context " + context);

            vertx.deployVerticle(VerticleToDeploy.class.getName(), new DeploymentOptions().setInstances(4),
                    asyncResultHandler);
        });

    }

    @Test(timeout = 2000)
    public void testContextPropagationForMultipleInstancesViaHandler(final TestContext testContext) throws Exception {

        final Vertx testVertx = rule.vertx();

        testVertx.eventBus().consumer("start.verticles").<String>handler(m ->
                testVertx.deployVerticle(VerticleToDeploy.class.getName(), new DeploymentOptions().setInstances(4),
                        result -> {
                            if (result.succeeded()) {
                                m.reply("All good");
                            } else {
                                throw new RuntimeException(result.cause());
                            }
                        }));

        final Verticle deployingVerticle = new AbstractVerticle() {
            @Override
            public void start(Future<Void> startFuture) throws Exception {
                final Context context = vertx.getOrCreateContext();
                System.out.println("Starting deploying verticle from context" + context);

                this.vertx.eventBus().send("start.verticles", "Hello", r -> {
                   if (r.succeeded()) {
                       startFuture.complete();
                   } else {
                       throw new RuntimeException(r.cause());
                   }
                });
            }
        };

        executeTest(testContext, (vertx, asyncResultHandler) -> vertx.deployVerticle(deployingVerticle, asyncResultHandler));
    }

    private void executeTest(final TestContext testContext, final BiConsumer<Vertx, Handler<AsyncResult<String>>> asyncTest) throws Exception {

        final Vertx vertx = rule.vertx();
        final Async async = testContext.async();

        final Handler<AsyncResult<String>> endTestHandler = result -> {
            if (result.succeeded()) {
                async.complete();
            } else {
                result.cause().printStackTrace();
                throw new RuntimeException("Failed to deploy verticles");
            }
        };

        // Do stuff here
        asyncTest.accept(vertx, endTestHandler);
    }

    public static class VerticleToDeploy extends AbstractVerticle {

        @Override
        public void start(Future<Void> startFuture) throws Exception {

            final Context context = vertx.getOrCreateContext();

            System.out.println("Starting verticle to deploy from context " + context);

            startFuture.complete();
        }
    }

}
