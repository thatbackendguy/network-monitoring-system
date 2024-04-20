package com.nms.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.nms.Main.LOGGER;

public class DatabaseConnection
{
    private DatabaseConnection(){}

    private static final String URL = "jdbc:mysql://localhost:3306/nmsDB";
    private static final String USER = "root";
    private static final String PASSWORD = "Root@1010";

    public static Connection getConnection()
    {
        Connection connection=null;

        try
        {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);

        } catch(SQLException e)
        {
            LOGGER.error("{}",e.getMessage());
        }

        return connection;

    }


}
