package com.nms;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    public static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static JsonArray PROVISION_DEVICES_LIST = new JsonArray();

    public static void main(String[] args)
    {
        Vertx vertx = Vertx.vertx();

        vertx.fileSystem().readFile("/home/yash/IdeaProjects/network-monitoring-system/provision_devices.json", readResult -> {
            if(readResult.succeeded())
            {
                Buffer buffer = readResult.result();

                PROVISION_DEVICES_LIST = buffer.toJsonArray();

                LOGGER.info("Provision devices file loaded successfully!");
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
