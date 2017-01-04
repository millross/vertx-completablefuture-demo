# vertx-completablefuture-demo

A simple demo of using completable futures with vert.x, covering the following three scenarios:-

* Completable future for non-blocking delayed result (comparable to using vert.x http client to make a rest call)
* Completable future for blocking result using executeBlocking
* Completable future where result is returned immediately and on-thread

Want to make sure that the result is used on the correct thread and correct context for
all these cases to ensure that it's a viable approach for pac4j-async underlying
APIs. I think it is, but would like to ensure this before I go too far down the
rabbithole when defining the APIs.