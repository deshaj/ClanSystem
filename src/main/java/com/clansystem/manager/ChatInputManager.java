package com.clansystem.manager;

import com.clansystem.ClanSystem;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInputManager {
    private final ClanSystem plugin;
    private final Map<UUID, ChatPrompt> awaitingInput = new ConcurrentHashMap<>();

    public ChatInputManager(ClanSystem plugin) {
        this.plugin = plugin;
    }

public void prompt(Player player, String messageKey, Consumer<String> callback) {
    awaitingInput.put(player.getUniqueId(), new ChatPrompt(callback));
    plugin.debug("ChatInput: Registered prompt for " + player.getName() + " (UUID: " + player.getUniqueId() + ")");
    plugin.debug("ChatInput: Awaiting map size: " + awaitingInput.size());
    plugin.getMessageManager().send(player, messageKey);
}

public boolean handleInput(Player player, String message) {
    plugin.debug("ChatInput: Handling input for " + player.getName() + " - Message: '" + message + "'");
    plugin.debug("ChatInput: Awaiting map contains player: " + awaitingInput.containsKey(player.getUniqueId()));
    
    ChatPrompt prompt = awaitingInput.remove(player.getUniqueId());
    if (prompt != null) {
        plugin.debug("ChatInput: Found prompt, executing callback...");
        try {
            prompt.getCallback().accept(message);
            plugin.debug("ChatInput: Callback executed successfully");
            return true;
        } catch (Exception e) {
            plugin.error("ChatInput: Error executing callback", e);
            return false;
        }
    }
    plugin.debug("ChatInput: No prompt found for player!");
    return false;
}

    public void cancel(UUID playerId) {
        awaitingInput.remove(playerId);
    }

public boolean isAwaiting(UUID playerId) {
    boolean awaiting = awaitingInput.containsKey(playerId);
    plugin.debug("ChatInput: isAwaiting(" + playerId + ") = " + awaiting);
    return awaiting;
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