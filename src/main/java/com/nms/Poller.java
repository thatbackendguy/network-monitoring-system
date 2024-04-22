package com.nms;

import com.nms.utils.DatabaseConnection;
import io.vertx.core.AbstractVerticle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.nms.Main.LOGGER;
import static com.nms.utils.Constants.*;

public class Poller extends AbstractVerticle
{

    private void storeToDB(ArrayList<String> output)
    {
        var cpuValues = output.get(1).split(" ");

        var memoryValues = output.get(3).split(" ");

        var swapMemoryValues = output.get(4).split(" ");

        if(cpuValues.length == 3 && memoryValues.length==3 && swapMemoryValues.length==3)
        {
            String sql = "INSERT INTO system_metrics (context_switches, free_memory, free_swap_memory, ip_address, load_average, idle_cpu_percentage, system_cpu_percentage, user_cpu_percentage, total_memory, total_swap_memory, used_memory, used_swap_memory) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try(var conn = DatabaseConnection.getConnection(); PreparedStatement stmt =conn.prepareStatement(sql);)
            {
                // Set the values for the placeholders
                stmt.setLong(1, Long.parseLong(output.get(0)));
                stmt.setLong(2, Long.parseLong(memoryValues[2]));
                stmt.setLong(3, Long.parseLong(swapMemoryValues[2]));
                stmt.setString(4, config().getString(IP_ADDRESS));
                stmt.setFloat(5, Float.parseFloat(output.get(2)));
                stmt.setFloat(6, Float.parseFloat(cpuValues[2]));
                stmt.setFloat(7, Float.parseFloat(cpuValues[1]));
                stmt.setFloat(8, Float.parseFloat(cpuValues[0]));
                stmt.setLong(9, Long.parseLong(memoryValues[0]));
                stmt.setLong(10, Long.parseLong(swapMemoryValues[0]));
                stmt.setLong(11, Long.parseLong(memoryValues[1]));
                stmt.setLong(12, Long.parseLong(swapMemoryValues[1]));

                // Execute the statement
                int rowsInserted = stmt.executeUpdate();

                LOGGER.info("{} rows inserted for {}", rowsInserted, config().getString(IP_ADDRESS));
            } catch(SQLException e)
            {
                LOGGER.error(e.getMessage());
            }
        }
    }


    @Override
    public void start() throws Exception
    {
        vertx.setPeriodic(10000, timerID -> {

            LOGGER.info("Polling for {}",config().getString(IP_ADDRESS));

            var command = " vmstat -s | grep 'context' | awk {'print $1'} ; mpstat | tail -1 | awk {'print $5,$7,$14'}; uptime | awk {'print $NF'}; free -m | awk {'print $2,$3,$4'} | tail -2";

            var polledBuffer = new ArrayList<String>(); // store result of terminal output

            try
            {

                ProcessBuilder processBuilder = new ProcessBuilder("sshpass", "-p", config().getString(PASSWORD), "ssh", "-o", "StrictHostKeyChecking=no", config().getString(USERNAME)+"@"+config().getString(IP_ADDRESS), command);

                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;

                while((line = reader.readLine()) != null)
                {
                    LOGGER.debug(line);
                    if(!line.contains("refused"))
                    {
                        polledBuffer.add(line);
                    }
                    else
                    {
                        LOGGER.info("Device down! Undeploy poller: {}", context.deploymentID());
                        vertx.cancelTimer(timerID);
                        vertx.undeploy(context.deploymentID());
                    }
                }

                int exitCode = process.waitFor();

                if(!polledBuffer.isEmpty() && exitCode == 0)
                {
                    storeToDB(polledBuffer);
                }
                else
                {
                    LOGGER.info("Device down! Undeploy poller: {}", context.deploymentID());
                    vertx.cancelTimer(timerID);
                    vertx.undeploy(context.deploymentID());
                }


            } catch(Exception e)
            {
                LOGGER.error(e.getMessage());
            }
        });
    }
}
