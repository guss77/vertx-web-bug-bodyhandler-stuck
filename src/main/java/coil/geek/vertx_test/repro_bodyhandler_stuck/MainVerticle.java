package coil.geek.vertx_test.repro_bodyhandler_stuck;

import java.time.*;
import java.util.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var server = vertx.createHttpServer();
    var router = Router.router(vertx);
    if (config().getBoolean("delay", false)) {
      System.out.println("Registering delay handler");
      router.post().handler(ctx -> {
        System.out.println("Delaying before body processing");
        // spend some time before calling the next handler
        new Timer().schedule(new TimerTask() { public void run() {
          ctx.next();
        } }, Date.from(Instant.now().plusMillis(50)));
      });
    }
    router.post().handler(BodyHandler.create());
    router.post().handler(ctx -> {
      ctx.response().end("Hello " + ctx.body().asString());
    });
    server.requestHandler(router).listen(config().getInteger("http.port", 8080))
    .onSuccess(__ -> startPromise.complete())
    .onFailure(startPromise::fail);
  }
}
