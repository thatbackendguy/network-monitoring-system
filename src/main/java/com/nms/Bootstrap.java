package com.nms;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nms.utils.Constants.PROVISION_DEVICES_JSON_FILE_PATH;

public class Bootstrap
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static JsonArray PROVISION_DEVICES_LIST = new JsonArray();

    public static void main(String[] args)
    {
        Vertx vertx = Vertx.vertx();

        vertx.fileSystem().readFile(PROVISION_DEVICES_JSON_FILE_PATH, readResult -> {
            if(readResult.succeeded())
            {
                Buffer buffer = readResult.result();

                PROVISION_DEVICES_LIST = buffer.toJsonArray();

                LOGGER.info("Provision devices file loaded successfully! Devices available: {}", PROVISION_DEVICES_LIST.size());
            }
            else
            {
                LOGGER.error("Cannot read provision devices file.");
            }
        });

        vertx.deployVerticle("com.nms.Server", handler -> {
            if(handler.succeeded())
            {
                LOGGER.info("Server verticle deployed");
            }
        });
    }
}
