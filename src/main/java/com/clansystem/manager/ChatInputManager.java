package com.clansystem.manager;

import com.clansystem.ClanSystem;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputManager {
    private final ClanSystem plugin;
    private final Map<UUID, ChatPrompt> awaitingInput = new HashMap<>();

    public ChatInputManager(ClanSystem plugin) {
        this.plugin = plugin;
    }

    public void prompt(Player player, String messageKey, Consumer<String> callback) {
        awaitingInput.put(player.getUniqueId(), new ChatPrompt(callback));
        plugin.getMessageManager().send(player, messageKey);
    }

    public boolean handleInput(Player player, String message) {
        ChatPrompt prompt = awaitingInput.remove(player.getUniqueId());
        if (prompt != null) {
            prompt.getCallback().accept(message);
            return true;
        }
        return false;
    }

    public void cancel(UUID playerId) {
        awaitingInput.remove(playerId);
    }

    public boolean isAwaiting(UUID playerId) {
        return awaitingInput.containsKey(playerId);
    }

    private static class ChatPrompt {
        private final Consumer<String> callback;

        public ChatPrompt(Consumer<String> callback) {
            this.callback = callback;
        }

        public Consumer<String> getCallback() {
            return callback;
        }
    }
}