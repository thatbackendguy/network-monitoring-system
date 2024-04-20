package com.nms;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    public static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle("com.nms.ServerVerticle", handler -> {
            if(handler.succeeded())
            {
                LOGGER.info("Server verticle deployed");
            }
        });
    }
}
