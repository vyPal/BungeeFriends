package com.vypalstudios.BungeeFriends;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vypalstudios.BungeeFriends.command.FriendCommand;
import com.vypalstudios.BungeeFriends.command.MessageCommand;
import com.vypalstudios.BungeeFriends.dao.FriendsQuery;
import com.vypalstudios.BungeeFriends.database.MySQLConnector;
import com.vypalstudios.BungeeFriends.i18n.I18NManager;
import com.vypalstudios.BungeeFriends.listener.ConnectionListener;
import com.vypalstudios.BungeeFriends.util.ConfigHelper;
import com.vypalstudios.BungeeFriends.util.MojangAPIHelper;
import net.md_5.bungee.api.plugin.Plugin;

// ------BungeeFriends------
// author: vyPal
// -------------------------
public class BungeeFriends extends Plugin {

    private ConfigHelper config;

    private MySQLConnector mysql;
    private FriendsQuery query;

    private MojangAPIHelper mojang;
    private I18NManager i18n;

    @Override
    public void onEnable() {
        try {
            mojang = new MojangAPIHelper(this);
            i18n = new I18NManager("messages");
            initConfig();
            initMySQL();
            registerListeners();
            registerCommands();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(BungeeFriends.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initConfig() throws IOException {
        config = new ConfigHelper("config", "config", this);
    }

    private void initMySQL() throws SQLException {
        mysql = new MySQLConnector(this);
        mysql.connect();
        query = new FriendsQuery(mysql);
    }

    private void registerListeners() {
        getProxy().getPluginManager().registerListener(this, new ConnectionListener(this));
    }

    private void registerCommands() {
        getProxy().getPluginManager().registerCommand(this, new FriendCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MessageCommand(this));
    }

    public ConfigHelper getConfig() {
        return config;
    }

    public MySQLConnector getMySQL() {
        return mysql;
    }

    public FriendsQuery getQuery() {
        return query;
    }

    public MojangAPIHelper getMojangAPIHelper() {
        return mojang;
    }
    
    public I18NManager getI18NManager(){
        return i18n;
    }
}
