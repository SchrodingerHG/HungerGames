package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import me.aymanisam.hungergames.handlers.SetSpawnHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LobbyReturnCommand implements CommandExecutor {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final SetSpawnHandler setSpawnHandler;
    private final ConfigHandler configHandler;

    public LobbyReturnCommand(HungerGames plugin, LangHandler langHandler, SetSpawnHandler setSpawnHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.setSpawnHandler = setSpawnHandler;
        this.configHandler = new ConfigHandler(plugin, langHandler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langHandler.getMessage(null, "no-server"));
            return true;
        }

        if (!(player.hasPermission("hungergames.leave"))) {
            sender.sendMessage(langHandler.getMessage(player, "no-permission"));
            return true;
        }

        String lobbyWorldName = (String) plugin.getConfig().get("lobby-world");

        if (player.getWorld().getName().equals(lobbyWorldName)) {
            player.sendMessage(langHandler.getMessage(player, "game.not-lobby"));
            return true;
        }

        setSpawnHandler.removePlayerFromSpawnPoint(player, player.getWorld());

        assert lobbyWorldName != null;
        World lobbyWorld = Bukkit.getWorld(lobbyWorldName);
        if (lobbyWorld != null) {
            player.teleport(lobbyWorld.getSpawnLocation());
        } else {
            plugin.getLogger().log(Level.SEVERE, "Could not find lobbyWorld [ " + lobbyWorldName + "]");
        }

        boolean spectating = configHandler.getWorldConfig(player.getWorld()).getBoolean("spectating");
        if (spectating) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        Map<String, Player> worldSpawnPointMap = setSpawnHandler.spawnPointMap.computeIfAbsent(player.getWorld(), k -> new HashMap<>());
        List<String> worldSpawnPoints = setSpawnHandler.spawnPoints.computeIfAbsent(player.getWorld(), k -> new ArrayList<>());

        for (Player onlinePlayer : player.getWorld().getPlayers()) {
            langHandler.getLangConfig(onlinePlayer);
            onlinePlayer.sendMessage(langHandler.getMessage(player, "game.left", player.getName(),
                    worldSpawnPointMap.size(), worldSpawnPoints.size()));
        }

        return true;
    }
}