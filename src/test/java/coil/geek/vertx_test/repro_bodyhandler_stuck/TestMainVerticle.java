package coil.geek.vertx_test.repro_bodyhandler_stuck;

import java.time.*;

import io.vertx.core.*;
import io.vertx.core.json.*;
import io.vertx.core.http.*;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @Test
  void testQuick(Vertx vertx, VertxTestContext ctx) throws Throwable {
    var client = vertx.createHttpClient();
    var options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", 18080));
    var app = new MainVerticle();
    System.out.println("Starting quick test");
    vertx.deployVerticle(app, options)
    .compose(__ -> client.request(HttpMethod.POST, 18080, "localhost", "/"))
    .compose(req -> req.send("world"))
    .compose(res -> res.body()
      .map(body -> {
        assertEquals(res.statusCode(), 200);
        assertEquals(body.toString(), "Hello world");
        return null;
      })
    )
    .onComplete(__ -> System.out.println("Finished quick test"))
    .onComplete(ctx.succeeding(id -> ctx.completeNow()));
  }

  @Test
  void testSlow(Vertx vertx, VertxTestContext ctx) throws Throwable {
    var client = vertx.createHttpClient();
    var options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", 18080)
        .put("delay", 10L));
    var app = new MainVerticle();
    System.out.println(Instant.now() + " Starting slow test");
    vertx.deployVerticle(app, options)
    .compose(__ -> client.request(HttpMethod.POST, 18080, "localhost", "/"))
    .compose(req -> req.setTimeout(10000).send("world"))
    .compose(res -> res.body()
    .map(body -> {
      assertEquals(res.statusCode(), 200);
      assertEquals(body.toString(), "Hello world");
      return null;
    })
    )
    .onComplete(__ -> System.out.println(Instant.now() + " Finished slow test"))
    .onComplete(ctx.succeeding(id -> ctx.completeNow()));
  }
}
