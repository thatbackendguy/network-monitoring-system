package com.nms;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.handler.ErrorHandler;

import static com.nms.Main.*;
import static com.nms.utils.Constants.*;

public class Server extends AbstractVerticle
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

            ctx.json(new JsonObject().put(STATUS, SUCCESS).put(MESSAGE, "Welcome to Network Monitoring System!"));
        });

        // POST: /register-device
        mainRouter.route(HttpMethod.POST, "/register-device").handler(ctx -> {

            LOGGER.info(REQ_CONTAINER, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            ctx.request().body(reqBuffer -> {
                var req = reqBuffer.result().toJsonObject();

                PROVISION_DEVICES_LIST.add(new JsonObject()
                        .put(USERNAME, req.getString(USERNAME))
                        .put(PASSWORD, req.getString(PASSWORD))
                        .put(IP_ADDRESS, req.getString(IP_ADDRESS))
                        .put(DEVICE_TYPE, req.getString(DEVICE_TYPE))
                );

                Buffer updatedBuffer = Buffer.buffer(PROVISION_DEVICES_LIST.toString());

                vertx.fileSystem().writeFile("/home/yash/IdeaProjects/network-monitoring-system/provision_devices.json", updatedBuffer, writeResult -> {
                    if(writeResult.succeeded())
                    {
                        LOGGER.info("File updated successfully");

                        ctx.json(new JsonObject().put(STATUS, SUCCESS).put(MESSAGE, "Device registered successfully"));
                    }
                    else
                    {
                        LOGGER.info("Failed to update file: {}", writeResult.cause().toString());

                        ctx.json(new JsonObject().put(STATUS, ERROR).put(MESSAGE, "Device did not register successfully"));
                    }
                });
            });
        });

        mainRouter.route(HttpMethod.GET, "/start-polling").handler(ctx -> {
            LOGGER.info(REQ_CONTAINER, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            int totalNoDevices = PROVISION_DEVICES_LIST.size();

            for(Object device: PROVISION_DEVICES_LIST)
            {
                JsonObject deviceJson = (JsonObject) device;

                vertx.deployVerticle("com.nms.Poller", new DeploymentOptions().setConfig(deviceJson).setThreadingModel(ThreadingModel.WORKER).setWorkerPoolSize(1).setInstances(1).setWorkerPoolName(config().getString(IP_ADDRESS)), handler -> {
                    if(handler.succeeded())
                    {
                        LOGGER.info("Polling started: {}",deviceJson.getString(IP_ADDRESS));
                    }
                });
            }

            ctx.json(new JsonObject().put(STATUS, SUCCESS).put(MESSAGE, "Polling started successfully! No. of devices: " + totalNoDevices));
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
