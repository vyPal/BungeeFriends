package com.vypalstudios.BungeeFriends.listener;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vypalstudios.BungeeFriends.BungeeFriends;
import com.vypalstudios.BungeeFriends.i18n.I18NManager;
import com.vypalstudios.BungeeFriends.util.MessageBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

// ------BungeeFriends------
// author: vyPal
// -------------------------
public class ConnectionListener implements Listener {

    private final BungeeFriends plugin;
    private final I18NManager i18n;

    public ConnectionListener(BungeeFriends plugin) {
        this.plugin = plugin;
        i18n = plugin.getI18NManager();
    }

    @EventHandler
    public void onLogin(PostLoginEvent e) {
        try {
            ProxiedPlayer player = e.getPlayer();
            plugin.getQuery().addPlayer(player);
            List<UUID> uuids = plugin.getQuery().getFriends(player);
            for (UUID uuid : uuids) {
                ProxiedPlayer friend = plugin.getProxy().getPlayer(uuid);
                if (friend != null && friend.isConnected() && plugin.getQuery().getSetting(friend.getUniqueId(), "togglenotify")) {
                    i18n.changeLocale(friend.getLocale());
                    new MessageBuilder(i18n).addReplacedText("notifyonline", "%PLAYER%", player.getName()).sendMessage(friend);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        try {
            ProxiedPlayer player = e.getPlayer();
            List<UUID> uuids = plugin.getQuery().getFriends(player);
            for (UUID uuid : uuids) {
                ProxiedPlayer friend = plugin.getProxy().getPlayer(uuid);
                if (friend != null && friend.isConnected() && plugin.getQuery().getSetting(friend.getUniqueId(), "togglenotify")) {
                    i18n.changeLocale(friend.getLocale());
                    new MessageBuilder(i18n).addReplacedText("notifyoffline", "%PLAYER%", player.getName()).sendMessage(friend);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
