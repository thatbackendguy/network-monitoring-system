package com.nms;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;

import static com.nms.Main.LOGGER;
import static com.nms.utils.Constants.*;

public class ServerVerticle extends AbstractVerticle
{
    private ErrorHandler errorHandler()
    {
        return ErrorHandler.create(vertx);
    }

    @Override
    public void start() throws Exception
    {
        HttpServer server = vertx.createHttpServer();

        Router mainRouter = Router.router(vertx);

        // for handling failures
        mainRouter.route().failureHandler(errorHandler());

        // GET: "/"
        mainRouter.route("/").handler(ctx -> {
            LOGGER.info(REQ_CONTAINER, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            ctx.json(new JsonObject().put("status", "success").put("message", "Welcome to NMS!"));
        });

        // POST: /register-device
        mainRouter.route(HttpMethod.POST, "/register-device").handler(ctx -> {
            LOGGER.info(REQ_CONTAINER, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

        });

        server.requestHandler(mainRouter).listen(8080, res -> {
            if(res.succeeded())
            {

                LOGGER.info("Server is now listening on http://localhost:8080/");
            }
            else
            {
                LOGGER.info("Failed to start the server");
            }
        });
    }
}
