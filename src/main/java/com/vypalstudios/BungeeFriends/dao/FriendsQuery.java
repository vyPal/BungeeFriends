package com.vypalstudios.BungeeFriends.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vypalstudios.BungeeFriends.database.MySQLConnector;
import net.md_5.bungee.api.connection.ProxiedPlayer;

// ------BungeeFriends------
// author: vyPal
// -------------------------
public class FriendsQuery {

    private final MySQLConnector mysql;

    public FriendsQuery(MySQLConnector mysql) {
        this.mysql = mysql;
    }

    public void addPlayer(ProxiedPlayer player) throws SQLException {
        String query = "INSERT IGNORE INTO friends_settings (uuid, togglerequests, togglenotify, togglemessages, togglejump) VALUES (?,?,?,?,?)";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, player.getUniqueId().toString());
        statement.setBoolean(2, true);
        statement.setBoolean(3, true);
        statement.setBoolean(4, true);
        statement.setBoolean(5, true);
        mysql.executeUpdate(statement);
    }

    public void toggleSetting(ProxiedPlayer player, String setting) throws SQLException {
        String query = "UPDATE friends_settings SET " + setting + "=? WHERE uuid=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setBoolean(1, !getSetting(player.getUniqueId(), setting));
        statement.setString(2, player.getUniqueId().toString());
        mysql.executeUpdate(statement);
    }
    
    public boolean getSetting(UUID uuid, String setting) throws SQLException {
        String query = "SELECT " + setting + " FROM friends_settings WHERE uuid=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, uuid.toString());
        ResultSet rs = mysql.executeQuery(statement);
        if (rs.first()) {
            return rs.getBoolean(1);
        }
        return false;
    }

    public boolean getPlayerExists(UUID uuid) throws SQLException {
        String query = "SELECT uuid FROM friends_settings WHERE uuid=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, uuid.toString());
        ResultSet rs = mysql.executeQuery(statement);
        return rs.first();
    }

    public List<UUID> getFriends(ProxiedPlayer player) throws SQLException {
        List<UUID> uuids = new ArrayList<>();
        
        String query = "SELECT uuidone FROM friends_friends WHERE uuidtwo=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, player.getUniqueId().toString());
        ResultSet rs = mysql.executeQuery(statement);
        while (rs.next()) {
            uuids.add(UUID.fromString(rs.getString(1)));
        }
        
        query = "SELECT uuidtwo FROM friends_friends WHERE uuidone=?";
        statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, player.getUniqueId().toString());
        rs = mysql.executeQuery(statement);
        while (rs.next()) {
            uuids.add(UUID.fromString(rs.getString(1)));
        }

        return uuids;
    }

    public boolean isFriend(UUID uuidone, UUID uuidtwo) throws SQLException {
        String query = "SELECT id FROM friends_friends WHERE uuidone=? AND uuidtwo=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, uuidone.toString());
        statement.setString(2, uuidtwo.toString());
        ResultSet rs = mysql.executeQuery(statement);
        boolean first = rs.first();

        query = "SELECT id FROM friends_friends WHERE uuidone=? AND uuidtwo=?";
        statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, uuidtwo.toString());
        statement.setString(2, uuidone.toString());
        rs = mysql.executeQuery(statement);

        return first || rs.first();
    }

    public void removeFriend(UUID uuidone, UUID uuidtwo) throws SQLException {
        String query = "DELETE FROM friends_friends WHERE uuidone=? AND uuidtwo=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, uuidone.toString());
        statement.setString(2, uuidtwo.toString());
        mysql.executeUpdate(statement);

        statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, uuidtwo.toString());
        statement.setString(2, uuidone.toString());
        mysql.executeUpdate(statement);
    }

    public void acceptRequest(ProxiedPlayer player, UUID uuid) throws SQLException {
        String query = "DELETE FROM friends_requests WHERE requester=? AND receiver=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, uuid.toString());
        statement.setString(2, player.getUniqueId().toString());
        mysql.executeUpdate(statement);

        query = "DELETE FROM friends_requests WHERE requester=? AND receiver=?";
        statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, uuid.toString());
        mysql.executeUpdate(statement);

        query = "INSERT INTO friends_friends (uuidone, uuidtwo) VALUES(?,?)";
        statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, uuid.toString());
        mysql.executeUpdate(statement);
    }

    public void denyRequest(ProxiedPlayer player, UUID uuid) throws SQLException {
        String query = "DELETE FROM friends_requests WHERE requester=? AND receiver=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, uuid.toString());
        statement.setString(2, player.getUniqueId().toString());
        mysql.executeUpdate(statement);
    }

    public void addRequest(ProxiedPlayer player, UUID uuid) throws SQLException {
        String query = "INSERT IGNORE INTO friends_requests (requester, receiver) VALUES (?,?)";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, uuid.toString());
        mysql.executeUpdate(statement);
    }

    public List<UUID> getRequests(ProxiedPlayer player) throws SQLException {
        String query = "SELECT requester FROM friends_requests WHERE receiver=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, player.getUniqueId().toString());
        ResultSet rs = mysql.executeQuery(statement);
        List<UUID> uuids = new ArrayList<>();
        int index = 1;
        while (rs.next()) {
            uuids.add(UUID.fromString(rs.getString(index)));
            index++;
        }
        return uuids;
    }

    public boolean getRequestExists(UUID sender, UUID receiver) throws SQLException {
        String query = "SELECT id FROM friends_requests WHERE requester=? AND receiver=?";
        PreparedStatement statement = mysql.getConnection().prepareStatement(query);
        statement.setString(1, sender.toString());
        statement.setString(2, receiver.toString());
        ResultSet rs = mysql.executeQuery(statement);
        return rs.first();
    }
}
