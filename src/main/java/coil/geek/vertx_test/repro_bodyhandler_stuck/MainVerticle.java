package coil.geek.vertx_test.repro_bodyhandler_stuck;

import java.time.*;

import io.vertx.core.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

	public void delay(Promise<Void> p, long ms) {
		new Thread(() -> {
			try {
				Thread.sleep(ms);
				System.out.println(Instant.now() + " Done delaying the processing");
				p.complete();
			} catch (Exception e) {
			}
		}).start();
	}

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var server = vertx.createHttpServer();
    var router = Router.router(vertx);
    var delay = config().getLong("delay", 0L);
    router.post().handler(ctx -> {
      System.out.println(Instant.now() + " Delaying before body processing");
      Promise<Void> p = Promise.promise();
      delay(p, delay);
      p.future().onSuccess(__ -> ctx.next());
    });
    router.post().handler(BodyHandler.create());
    router.post().handler(ctx -> {
      ctx.response().end("Hello " + ctx.body().asString());
    });
    router.route().failureHandler(ctx -> {
      System.err.println(Instant.now() + " Handling failure: " + ctx.statusCode() + " " + ctx.failure());
      ctx.response().setStatusCode(500).end("Error: " + ctx.failure());
    });
    router.errorHandler(500, ctx -> {
      System.err.println(Instant.now() + " Handling error: " + ctx.statusCode() + " " + ctx.failure());
      ctx.response().setStatusCode(500).end("Error: " + ctx.failure());
    });
    server.requestHandler(router).listen(config().getInteger("http.port", 8080))
    .onSuccess(__ -> startPromise.complete())
    .onFailure(startPromise::fail);
  }
}
