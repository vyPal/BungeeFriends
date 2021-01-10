package com.vypalstudios.BungeeFriends.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vypalstudios.BungeeFriends.BungeeFriends;
import net.md_5.bungee.api.connection.ProxiedPlayer;

// ------BungeeFriends------
// author: vyPal
// -------------------------
public class MojangAPIHelper {

    private final Cache<String, UUID> names;
    private final Cache<UUID, String> uuids;
    private final Gson gson;
    private final ExecutorService executor;
    private final BungeeFriends plugin;

    public MojangAPIHelper(BungeeFriends plugin) {
        gson = new Gson();
        names = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
        uuids = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
        executor = Executors.newCachedThreadPool();
        this.plugin = plugin;
    }

    public String getName(UUID uuid) throws IOException {
        ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
        if (player != null && player.isConnected()) {
            uuids.put(uuid, player.getName());
            names.put(player.getName(), uuid);
            return player.getName();
        }
        String name = uuids.getIfPresent(uuid);
        if (name != null) {
            uuids.put(uuid, name);
            names.put(name, uuid);
            return name;
        }
        String linkuuid = uuid.toString().replace("-", "");
        String output = getHttpGetContent("https://sessionserver.mojang.com/session/minecraft/profile/" + linkuuid);
        JsonObject jsonobj = gson.fromJson(output, JsonObject.class);
        if (jsonobj.has("name")) {
            uuids.put(uuid, jsonobj.get("name").getAsString());
            names.put(jsonobj.get("name").getAsString(), uuid);
            return jsonobj.get("name").getAsString();
        } else {
            throw new UnsupportedOperationException("Mojang API Cooldown Failure!");
        }
    }

    public UUID getUUID(String username) throws IOException {
        ProxiedPlayer player = plugin.getProxy().getPlayer(username);
        if (player != null && player.isConnected()) {
            uuids.put(player.getUniqueId(), player.getName());
            names.put(player.getName(), player.getUniqueId());
            return player.getUniqueId();
        }

        UUID uuid = names.getIfPresent(username);
        if (uuid != null) {
            uuids.put(uuid, username);
            names.put(username, uuid);
            return uuid;
        }

        String output = getHttpGetContent("https://api.mojang.com/users/profiles/minecraft/" + username);
        if (output != null) {
            JsonObject jsonobj = gson.fromJson(output, JsonObject.class);
            String newuuid = jsonobj.get("id").getAsString()
                    .replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
            uuids.put(UUID.fromString(newuuid), username);
            names.put(username, UUID.fromString(newuuid));
            return UUID.fromString(newuuid);
        }
        return null;
    }

    private String getHttpGetContent(String get) throws IOException {
        Future<String> future = executor.submit(() -> {
            URL url = new URL(get);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (connection.getResponseCode() != 200) {
                return null;
            }
            InputStreamReader input = new InputStreamReader(connection.getInputStream());
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(input)) {
                String output;
                while ((output = reader.readLine()) != null) {
                    builder.append(output);
                }
            }
            return builder.toString();
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MojangAPIHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
