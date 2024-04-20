package com.nms;

import com.nms.utils.DatabaseConnection;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.handler.ErrorHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

        mainRouter.route(HttpMethod.GET,"/get-data/:ip_address").handler(ctx-> {
            LOGGER.info(REQ_CONTAINER, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            var response = new JsonObject();

            var contextSwitches = new JsonArray();
            var freeMemory = new JsonArray();
            var freeSwapMemory = new JsonArray();
            var loadAvg = new JsonArray();
            var idlePercentage = new JsonArray();
            var systemPercentage = new JsonArray();
            var userPercentage = new JsonArray();
            var pollTimestamp = new JsonArray();
            var totalMemory = new JsonArray();
            var totalSwapMemory = new JsonArray();
            var usedMemory = new JsonArray();
            var usedSwapMemory = new JsonArray();

           var ipAddress = ctx.request().getParam(IP_ADDRESS);
           vertx.executeBlocking(()->{
                try(Connection conn = DatabaseConnection.getConnection();)
                {
                    String selectQuery = "SELECT * FROM system_metrics WHERE ip_address=?";

                    PreparedStatement stmt = conn.prepareStatement(selectQuery);

                    stmt.setString(1,ipAddress);

                    ResultSet rs = stmt.executeQuery();

                    while(rs.next())
                    {
                        contextSwitches.add(rs.getString(CONTEXT_SWITCHES));
                        freeMemory.add(rs.getString(FREE_MEMORY));
                        freeSwapMemory.add(rs.getString(FREE_SWAP_MEMORY));
                        loadAvg.add(rs.getString(LOAD_AVERAGE));
                        idlePercentage.add(rs.getString(IDLE_CPU_PERCENTAGE));
                        systemPercentage.add(rs.getString(SYSTEM_CPU_PERCENTAGE));
                        userPercentage.add(rs.getString(USER_CPU_PERCENTAGE));
                        pollTimestamp.add(rs.getString(POLL_TIMESTAMP));
                        totalMemory.add(rs.getString(TOTAL_MEMOEY));
                        totalSwapMemory.add(rs.getString(TOTAL_SWAP_MEMORY));
                        usedMemory.add(rs.getString(USED_MEMORY));
                        usedSwapMemory.add(rs.getString(USED_SWAP_MEMORY));
                    }

                    response.put(IP_ADDRESS,ipAddress)
                            .put(CONTEXT_SWITCHES,contextSwitches)
                            .put(FREE_MEMORY,freeMemory)
                            .put(FREE_SWAP_MEMORY,freeSwapMemory)
                            .put(LOAD_AVERAGE,loadAvg)
                            .put(IDLE_CPU_PERCENTAGE,idlePercentage)
                            .put(SYSTEM_CPU_PERCENTAGE,systemPercentage)
                            .put(USERNAME,userPercentage)
                            .put(POLL_TIMESTAMP,pollTimestamp)
                            .put(TOTAL_MEMOEY,totalMemory)
                            .put(TOTAL_SWAP_MEMORY,totalSwapMemory)
                            .put(USED_MEMORY,usedMemory)
                            .put(USED_SWAP_MEMORY,usedSwapMemory);
                }
                catch(SQLException e)
                {
                    LOGGER.error(e.getMessage());
                }

               return response;
           }).onComplete(res -> {
               ctx.json(res.result());
               LOGGER.info("Response sent successfully!");
           });
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
