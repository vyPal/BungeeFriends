package com.vypalstudios.BungeeFriends.database;

import com.vypalstudios.BungeeFriends.BungeeFriends;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

// ------BungeeFriends------
// author: vyPal
// -------------------------
public class MySQLConnector {

    private final String username;
    private final String password;
    private final String host;
    private final String database;
    private final int port;
    private Connection connection;

    private final BungeeFriends plugin;
    private final ExecutorService executor;

    public MySQLConnector(BungeeFriends plugin) {
        this.plugin = plugin;
        username = plugin.getConfig().getConfig().getString("MySQL.username");
        password = plugin.getConfig().getConfig().getString("MySQL.password");
        host = plugin.getConfig().getConfig().getString("MySQL.host");
        database = plugin.getConfig().getConfig().getString("MySQL.database");
        port = plugin.getConfig().getConfig().getInt("MySQL.port");
        executor = Executors.newCachedThreadPool();
    }

    public ResultSet executeQuery(PreparedStatement statement) {
        if (connection != null) {
            Future<ResultSet> future = executor.submit(() -> {
                try {
                    return statement.executeQuery();
                } catch (SQLException ex) {
                    Logger.getLogger(MySQLConnector.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            });
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(MySQLConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public void executeUpdate(PreparedStatement statement) {
        if (connection != null) {
            plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                try {
                    statement.execute();
                } catch (SQLException ex) {
                    Logger.getLogger(MySQLConnector.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    public void connect() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        }
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }

}
