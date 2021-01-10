package com.vypalstudios.BungeeFriends.util;

import com.vypalstudios.BungeeFriends.i18n.I18NManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

// ------BungeeFriends------
// author: vyPal
// -------------------------
public class MessageBuilder {
    
    private final I18NManager i18n;
    private final TextComponent text;
    
    public MessageBuilder(I18NManager i18n) {
        this.i18n = i18n;
        text = new TextComponent(i18n.getMessage("prefix"));
    }
    
    public MessageBuilder addClickMessage(String path, ClickEvent.Action action, String actionValue) {
        TextComponent add = new TextComponent(i18n.getMessage(path));
        add.setClickEvent(new ClickEvent(action, actionValue));
        text.addExtra(add);
        return this;
    }
    
    public MessageBuilder addHoverMessage(String path, HoverEvent.Action action, String actionValue) {
        TextComponent add = new TextComponent(i18n.getMessage(path));
        add.setHoverEvent(new HoverEvent(action, new ComponentBuilder(actionValue).create()));
        text.addExtra(add);
        return this;
    }
    
    public MessageBuilder addText(String path) {
        text.addExtra(new TextComponent(i18n.getMessage(path)));
        return this;
    }
    
    public MessageBuilder addReplacedText(String path, String target, String replacement) {
        String raw = i18n.getMessage(path).replace(target, replacement);
        text.addExtra(new TextComponent(raw));
        return this;
    }
    
    public TextComponent getText() {
        return text;
    }
    
    public void sendMessage(ProxiedPlayer player) {
        player.sendMessage(text);
    }
}
