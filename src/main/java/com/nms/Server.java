package com.nms;

import com.nms.utils.DatabaseConnection;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ErrorHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.nms.Main.*;
import static com.nms.utils.Constants.*;

public class Server extends AbstractVerticle
{
    private ErrorHandler errorHandler()
    {
        return ErrorHandler.create(vertx);
    }

    private void storeToDB(ArrayList<String> output, JsonObject deviceJson)
    {
        var cpuValues = output.get(1).split(" ");

        var memoryValues = output.get(3).split(" ");

        var swapMemoryValues = output.get(4).split(" ");

        if(cpuValues.length == 3 && memoryValues.length == 3 && swapMemoryValues.length == 3)
        {
            String sql = "INSERT INTO system_metrics (context_switches, free_memory, free_swap_memory, ip_address, load_average, idle_cpu_percentage, system_cpu_percentage, user_cpu_percentage, total_memory, total_swap_memory, used_memory, used_swap_memory) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try(var conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql);)
            {
                // Set the values for the placeholders
                stmt.setString(1, output.get(0)); // context-switches
                stmt.setString(2, memoryValues[1]); // free-memory
                stmt.setString(3, swapMemoryValues[1]); // free-swap-memory
                stmt.setString(4, deviceJson.getString(IP_ADDRESS)); // ip-address
                stmt.setString(5, output.get(2)); // load_avg
                stmt.setString(6, cpuValues[2]); // idle-cpu
                stmt.setString(7, cpuValues[1]); // sys-cpu
                stmt.setString(8, cpuValues[0]); // user-cpu
                stmt.setString(9, memoryValues[0]); // total-memory
                stmt.setString(10, swapMemoryValues[0]); // total-swap-memory
                stmt.setString(11, memoryValues[2]); // used-memory
                stmt.setString(12, swapMemoryValues[2]); // used-swap-memory

                // Execute the statement
                int rowsInserted = stmt.executeUpdate();

                LOGGER.info("{} rows inserted for {}", rowsInserted, deviceJson.getString(IP_ADDRESS));
            } catch(SQLException e)
            {
                LOGGER.error(e.getMessage());
            }
        }
    }

    @Override
    public void start() throws Exception
    {
        HttpServer server = vertx.createHttpServer();

        Router mainRouter = Router.router(vertx);

        mainRouter.route().handler(CorsHandler.create().addOrigin("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST));

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

                PROVISION_DEVICES_LIST.add(new JsonObject().put(USERNAME, req.getString(USERNAME)).put(PASSWORD, req.getString(PASSWORD)).put(IP_ADDRESS, req.getString(IP_ADDRESS)).put(DEVICE_TYPE, req.getString(DEVICE_TYPE)));

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


            for(Object device : PROVISION_DEVICES_LIST)
            {
                JsonObject deviceJson = (JsonObject) device;

                vertx.setPeriodic(10000, timerID -> {
                    vertx.executeBlocking(f -> {
                        LOGGER.info("Polling for {}", deviceJson.getString(IP_ADDRESS));

                        var command = " vmstat -s | grep 'context' | awk {'print $1'} ; top -bn1 | head -n 3 | tail -n 1 | awk {'print $2,$4,$8'}; uptime | awk {'print $NF'};top -bn1 | head -n 4 | tail -n 1 | awk {'print $4,$6,$8'}; top -bn1 | head -n 5 | tail -n 1 | awk {'print $3,$5,$7'};";

                        var polledBuffer = new ArrayList<String>(); // store result of terminal output

                        try
                        {

                            ProcessBuilder processBuilder = new ProcessBuilder("sshpass", "-p", deviceJson.getString(PASSWORD), "ssh", "-o", "StrictHostKeyChecking=no", deviceJson.getString(USERNAME) + "@" + deviceJson.getString(IP_ADDRESS), command);

                            processBuilder.redirectErrorStream(true);

                            Process process = processBuilder.start();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                            String line;

                            while((line = reader.readLine()) != null)
                            {
                                if(!line.contains("refused"))
                                {
                                    polledBuffer.add(line);
                                }
                                else
                                {
                                    LOGGER.info("Device down: {}", deviceJson.getString(IP_ADDRESS));
                                }
                            }

                            int exitCode = process.waitFor();

                            if(!polledBuffer.isEmpty() && exitCode == 0)
                            {
                                storeToDB(polledBuffer, deviceJson);
                            }
                            else
                            {
                                LOGGER.info("Device down: {}", deviceJson.getString(IP_ADDRESS));
                            }

                            f.complete("Poll complete for: " + deviceJson.getString(IP_ADDRESS));
                        } catch(Exception e)
                        {
                            LOGGER.error(e.getMessage());
                        }

                    }, res -> LOGGER.info(res.result().toString()));
                });

            }

            ctx.json(new JsonObject().put(STATUS, SUCCESS).put(MESSAGE, "Polling started successfully! No. of devices: " + totalNoDevices));

        });

        mainRouter.route(HttpMethod.GET, "/get-data/:ip_address").handler(ctx -> {
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
            vertx.executeBlocking(() -> {
                try(Connection conn = DatabaseConnection.getConnection();)
                {
                    LOGGER.info("Fetching data from DB");

                    String selectQuery = "SELECT * FROM (SELECT * FROM nmsDB.system_metrics WHERE ip_address=? ORDER BY poll_timestamp DESC LIMIT 60) AS latest_60 ORDER BY poll_timestamp ASC;";

                    PreparedStatement stmt = conn.prepareStatement(selectQuery);

                    stmt.setString(1, ipAddress);

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

                    response.put(IP_ADDRESS, ipAddress).put(CONTEXT_SWITCHES, contextSwitches).put(FREE_MEMORY, freeMemory).put(FREE_SWAP_MEMORY, freeSwapMemory).put(LOAD_AVERAGE, loadAvg).put(IDLE_CPU_PERCENTAGE, idlePercentage).put(SYSTEM_CPU_PERCENTAGE, systemPercentage).put(USER_CPU_PERCENTAGE, userPercentage).put(POLL_TIMESTAMP, pollTimestamp).put(TOTAL_MEMOEY, totalMemory).put(TOTAL_SWAP_MEMORY, totalSwapMemory).put(USED_MEMORY, usedMemory).put(USED_SWAP_MEMORY, usedSwapMemory);
                } catch(SQLException e)
                {
                    LOGGER.error(e.getMessage());
                }

                return response;
            }).onComplete(res -> {
                ctx.json(res.result());
                LOGGER.info("Response sent successfully!");
            });
        });

        mainRouter.route(HttpMethod.GET, "/get-ip-address").handler(ctx -> {
            LOGGER.info(REQ_CONTAINER, ctx.request().method(), ctx.request().path(), ctx.request().remoteAddress());

            var response = new JsonObject();

            var ipAddresses = new JsonArray();

            vertx.executeBlocking(() -> {
                try(Connection conn = DatabaseConnection.getConnection();)
                {
                    String selectQuery = "SELECT distinct(ip_address) FROM system_metrics";

                    PreparedStatement stmt = conn.prepareStatement(selectQuery);

                    ResultSet rs = stmt.executeQuery();

                    while(rs.next())
                    {
                        ipAddresses.add(rs.getString(IP_ADDRESS));
                    }

                    response.put(IP_ADDRESS, ipAddresses);
                } catch(SQLException e)
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
