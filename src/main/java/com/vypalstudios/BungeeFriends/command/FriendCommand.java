package com.vypalstudios.BungeeFriends.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vypalstudios.BungeeFriends.BungeeFriends;
import com.vypalstudios.BungeeFriends.i18n.I18NManager;
import com.vypalstudios.BungeeFriends.util.MessageBuilder;
import com.vypalstudios.BungeeFriends.util.MojangAPIHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

// ------BungeeFriends------
// author: vyPal
// -------------------------
public class FriendCommand extends Command {

    private final I18NManager i18n;
    private final BungeeFriends plugin;
    private final MojangAPIHelper mojang;

    public FriendCommand(BungeeFriends plugin) {
        super("friend", "friends.default", "friends", "f");
        i18n = plugin.getI18NManager();
        mojang = plugin.getMojangAPIHelper();
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) cs;
            i18n.changeLocale(player.getLocale());
            try {
                switch (args.length) {
                    case 1:
                        switch (args[0]) {
                            case "togglerequests":
                            case "togglenotify":
                            case "togglemessages":
                            case "togglejump":
                                toggleSetting(player, args[0]);
                                break;
                            case "requests":
                                displayRequests(player);
                                break;
                            case "list":
                                displayFriends(player);
                                break;
                            case "2":
                                sendSecondHelpMessage(player);
                                break;
                            default:
                                sendHelpMessage(player);
                                break;
                        }
                        break;
                    case 2:
                        switch (args[0]) {
                            case "add":
                                addFriend(player, args[1]);
                                break;
                            case "remove":
                                removeFriend(player, args[1]);
                                break;
                            case "accept":
                                acceptRequest(player, args[1]);
                                break;
                            case "deny":
                                denyRequest(player, args[1]);
                                break;
                            case "jump":
                                jumpToPlayer(player, args[1]);
                                break;
                            default:
                                sendHelpMessage(player);
                                break;
                        }
                        break;
                    default:
                        sendHelpMessage(player);
                        break;
                }
            } catch (SQLException | IOException ex) {
                Logger.getLogger(FriendCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void jumpToPlayer(ProxiedPlayer player, String name) throws IOException, SQLException {
        ProxiedPlayer jumpTo = plugin.getProxy().getPlayer(name);
        if (jumpTo != null && jumpTo.isConnected()) {
            UUID uuid = mojang.getUUID(name);
            if (!player.getName().equalsIgnoreCase(name)) {
                if (plugin.getQuery().isFriend(player.getUniqueId(), uuid)) {
                    if (plugin.getQuery().getSetting(uuid, "togglejump")) {
                        if (!player.getServer().equals(jumpTo.getServer())) {
                            player.connect(jumpTo.getServer().getInfo());
                        } else {
                            new MessageBuilder(i18n).addText("jumpsameserver").sendMessage(player);
                        }
                    } else {
                        new MessageBuilder(i18n).addReplacedText("jumpnotpossible", "%PLAYER%", jumpTo.getName()).sendMessage(player);
                    }
                } else {
                    new MessageBuilder(i18n).addReplacedText("notfriend", "%PLAYER%", jumpTo.getName()).sendMessage(player);
                }
            } else {
                new MessageBuilder(i18n).addText("jumpself").sendMessage(player);
            }
        } else {
            new MessageBuilder(i18n).addReplacedText("notonline", "%PLAYER%", name).sendMessage(player);
        }
    }

    private void displayFriends(ProxiedPlayer player) throws SQLException, IOException {
        List<UUID> uuids = plugin.getQuery().getFriends(player);
        Set<String> online = new TreeSet<>();
        Set<String> offline = new TreeSet<>();

        for (UUID uuid : uuids) {
            if (plugin.getProxy().getPlayer(uuid) != null && plugin.getProxy().getPlayer(uuid).isConnected()) {
                online.add(mojang.getName(uuid));
            } else {
                offline.add(mojang.getName(uuid));
            }
        }

        new MessageBuilder(i18n).addText("friendlist")
                .addReplacedText("friendsize1", "%COUNT%", online.size() + "")
                .addReplacedText("friendsize2", "%COUNT%", uuids.size() + "")
                .sendMessage(player);

        for (String str : online) {
            new MessageBuilder(i18n).addReplacedText("friendlist1", "%PLAYER%", str)
                    .addText("friendlistonline")
                    .sendMessage(player);
        }

        for (String str : offline) {
            new MessageBuilder(i18n).addReplacedText("friendlist1", "%PLAYER%", str)
                    .addText("friendlistoffline")
                    .sendMessage(player);
        }
    }

    private void removeFriend(ProxiedPlayer player, String name) throws IOException, SQLException {
        UUID uuid = mojang.getUUID(name);
        if (uuid != null && plugin.getQuery().isFriend(uuid, player.getUniqueId())) {
            plugin.getQuery().removeFriend(player.getUniqueId(), uuid);
            new MessageBuilder(i18n).addReplacedText("friendremove", "%PLAYER%", name).sendMessage(player);
            ProxiedPlayer other = plugin.getProxy().getPlayer(name);
            if (other != null && other.isConnected()) {
                new MessageBuilder(i18n).addReplacedText("friendremove", "%PLAYER%", player.getName()).sendMessage(other);
            }
        } else {
            new MessageBuilder(i18n).addReplacedText("friendnot", "%PLAYER%", name).sendMessage(player);
        }
    }

    private void denyRequest(ProxiedPlayer player, String name) throws IOException, SQLException {
        UUID uuid = mojang.getUUID(name);
        if (uuid != null && plugin.getQuery().getRequestExists(uuid, player.getUniqueId())) {
            plugin.getQuery().denyRequest(player, uuid);
            new MessageBuilder(i18n).addReplacedText("requestdenyself", "%PLAYER%", name).sendMessage(player);
            ProxiedPlayer other = plugin.getProxy().getPlayer(name);
            if (other != null && other.isConnected()) {
                new MessageBuilder(i18n).addReplacedText("requestdeny", "%PLAYER%", player.getName()).sendMessage(other);
            }
        } else {
            new MessageBuilder(i18n).addReplacedText("requestnotfound", "%PLAYER%", name).sendMessage(player);
        }
    }

    private void acceptRequest(ProxiedPlayer player, String name) throws IOException, SQLException {
        UUID uuid = mojang.getUUID(name);
        if (uuid != null && plugin.getQuery().getRequestExists(uuid, player.getUniqueId())) {
            plugin.getQuery().acceptRequest(player, uuid);
            new MessageBuilder(i18n).addReplacedText("requestaccepted", "%PLAYER%", name).sendMessage(player);
            ProxiedPlayer other = plugin.getProxy().getPlayer(name);
            if (other != null && other.isConnected()) {
                new MessageBuilder(i18n).addReplacedText("requestaccepted", "%PLAYER%", player.getName()).sendMessage(other);
            }
        } else {
            new MessageBuilder(i18n).addReplacedText("requestnotfound", "%PLAYER%", name).sendMessage(player);
        }
    }

    private void displayRequests(ProxiedPlayer player) throws SQLException, IOException {
        List<UUID> uuids = plugin.getQuery().getRequests(player);
        if (uuids.isEmpty()) {
            new MessageBuilder(i18n).addReplacedText("requestnosize", "%COUNT%", uuids.size() + "").sendMessage(player);
        } else if (uuids.size() == 1) {
            new MessageBuilder(i18n).addReplacedText("requestsize1", "%COUNT%", uuids.size() + "").sendMessage(player);
        } else {
            new MessageBuilder(i18n).addReplacedText("requestsize2", "%COUNT%", uuids.size() + "").sendMessage(player);
        }
        for (UUID uuid : uuids) {
            String name = mojang.getName(uuid);
            new MessageBuilder(i18n).addReplacedText("requestlist", "%PLAYER%", name).sendMessage(player);
        }
    }

    private void addFriend(ProxiedPlayer player, String name) throws IOException, SQLException {
        UUID uuid = mojang.getUUID(name);
        if (!player.getName().equalsIgnoreCase(name)) {
            if (uuid != null && plugin.getQuery().getPlayerExists(uuid)) {
                if (!plugin.getQuery().isFriend(player.getUniqueId(), uuid)) {
                    if (plugin.getQuery().getSetting(uuid, "togglerequests")) {
                        if (!plugin.getQuery().getRequestExists(player.getUniqueId(), uuid)) {
                            plugin.getQuery().addRequest(player, uuid);
                            new MessageBuilder(i18n).addReplacedText("requestsent", "%PLAYER%", name).sendMessage(player);
                            ProxiedPlayer receiver = plugin.getProxy().getPlayer(uuid);
                            if (receiver != null && receiver.isConnected()) {
                                new MessageBuilder(i18n).addReplacedText("requestget", "%PLAYER%", player.getName()).sendMessage(receiver);
                            }
                        } else {
                            new MessageBuilder(i18n).addText("requestedalready").sendMessage(player);
                        }
                    } else {
                        new MessageBuilder(i18n).addText("requestnotpossible").sendMessage(player);
                    }
                } else {
                    new MessageBuilder(i18n).addReplacedText("friendalready", "%PLAYER%", name).sendMessage(player);
                }
            } else {
                new MessageBuilder(i18n).addText("playernotfound").sendMessage(player);
            }
        } else {
            new MessageBuilder(i18n).addText("requestself").sendMessage(player);
        }
    }

    private void toggleSetting(ProxiedPlayer player, String setting) throws SQLException {
        plugin.getQuery().toggleSetting(player, setting);
        if (plugin.getQuery().getSetting(player.getUniqueId(), setting)) {
            new MessageBuilder(i18n).addText(setting + "1").sendMessage(player);
        } else {
            new MessageBuilder(i18n).addText(setting + "0").sendMessage(player);
        }
    }

    private void sendHelpMessage(ProxiedPlayer player) {
        new MessageBuilder(i18n).addText("helpline1").sendMessage(player);

        new MessageBuilder(i18n).addText("helpline2").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline3").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline4").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline5").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline6").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline7").sendMessage(player);
        new MessageBuilder(i18n).addText("separator").sendMessage(player);
    }

    private void sendSecondHelpMessage(ProxiedPlayer player) {
        new MessageBuilder(i18n).addText("helpline8").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline9").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline10").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline11").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline12").sendMessage(player);
        new MessageBuilder(i18n).addText("helpline13").sendMessage(player);
        new MessageBuilder(i18n).addText("separator").sendMessage(player);
    }

}
