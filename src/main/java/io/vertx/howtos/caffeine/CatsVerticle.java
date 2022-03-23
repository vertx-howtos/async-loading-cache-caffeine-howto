package io.vertx.howtos.caffeine;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.StaticHandler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;

import static io.vertx.core.http.HttpMethod.GET;

public class CatsVerticle extends AbstractVerticle {

  private static final Logger log = Logger.getLogger(CatsVerticle.class.getName());

  private HttpRequest<Buffer> request;
  private AsyncLoadingCache<Integer, Buffer> cache;

  @Override
  public void start() {
    // tag::web-client[]
    request = WebClient.create(vertx)
      .request(GET, new RequestOptions().setHost("http.cat").setPort(443).setSsl(true))
      .as(BodyCodec.buffer())
      .expect(ResponsePredicate.SC_OK);
    // end::web-client[]

    // tag::caffeine[]
    cache = Caffeine.newBuilder() // <1>
      .expireAfterWrite(Duration.ofMinutes(1)) // <2>
      .recordStats() // <3>
      .executor(cmd -> context.runOnContext(v -> cmd.run())) // <4>
      .buildAsync((key, exec) -> CompletableFuture.supplyAsync(() -> { // <5>
        Future<Buffer> future = fetchCatImage(key); // <6>
        return future.toCompletionStage(); // <7>
      }, exec).thenComposeAsync(Function.identity(), exec));

    vertx.setPeriodic(20000, l -> { // <8>
      CacheStats stats = cache.synchronous().stats();
      log.info("Stats: " + stats);
    });
    // end::caffeine[]

    // tag::server[]
    Router router = Router.router(vertx);
    router.get("/api/cats/:id").produces("image/*").handler(this::handleImageRequest);
    router.get().handler(StaticHandler.create());

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080);

    log.info("Server started on port 8080");
    // end::server[]
  }

  @SuppressWarnings("unused")
  private void simpleCacheSetup() {
    // tag::caffeine-simple[]
    cache = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(1))
      .recordStats()
      .buildAsync((key, exec) -> {
        Future<Buffer> future = fetchCatImage(key);
        return future.toCompletionStage().toCompletableFuture();
      });
    // end::caffeine-simple[]
  }

  // tag::handle-request[]
  private void handleImageRequest(RoutingContext rc) {
    Integer code = Integer.valueOf(rc.pathParam("id")); // <1>
    CompletableFuture<Buffer> completableFuture = cache.get(code); // <2>
    Future<Buffer> future = Future.fromCompletionStage(completableFuture, context); // <3>
    future.onComplete(ar -> { // <4>
      if (ar.succeeded()) {
        rc.response()
          .putHeader("Cache-Control", "no-store") // <5>
          .end(ar.result());
      } else {
        rc.fail(ar.cause());
      }
    });
  }
  // end::handle-request[]

  // tag::fetch[]
  private Future<Buffer> fetchCatImage(int code) {
    return request.uri("/" + code)
      .send()
      .map(HttpResponse::body);
  }
  // end::fetch[]

  // tag::main[]
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx(); // <1>
    vertx.deployVerticle(new CatsVerticle()); // <2>
  }
  // end::main[]
}
