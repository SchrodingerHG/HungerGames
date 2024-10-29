package me.aymanisam.hungergames.commands;

import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.aymanisam.hungergames.HungerGames.customTeams;

public class TeamSetCommand implements CommandExecutor {
    private final LangHandler langHandler;

    public TeamSetCommand(LangHandler langHandler) {
        this.langHandler = langHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length < 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(langHandler.getMessage(null, "team.no-args"));
            } else {
                sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-args"));
            }
            return true;
        }

        String action = args[0];

        if (action.equalsIgnoreCase("list")) {
            for (Map.Entry<String, List<Player>> entry : customTeams.entrySet()) {
                String team = entry.getKey();
                List<Player> members = entry.getValue();
                List<String> memberNames = members.stream().map(Player::getName).collect(Collectors.toList());
                sender.sendMessage(team + ": " + String.join(", ", memberNames));
            }
            if (customTeams.isEmpty()) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(langHandler.getMessage(null, "team.no-list"));
                } else {
                    sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-list"));
                }
            }
            return true;
        } else if (action.equalsIgnoreCase("reset")) {
            customTeams.clear();
            sender.sendMessage(langHandler.getMessage((Player) sender, "team.reset"));
            return true;
        }

        if (args.length < 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(langHandler.getMessage(null, "team.no-args"));
            } else {
                sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-args"));
            }
            return true;
        }

        String teamName = args[1];
        String playerName = args[2];

        Player player = Bukkit.getPlayer(playerName);
        if (player == null || (sender instanceof Player && player.getWorld() != ((Player) sender).getWorld())) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(langHandler.getMessage(null, "spectate.null-player"));
            } else {
                sender.sendMessage(langHandler.getMessage((Player) sender, "spectate.null-player"));
            }
            return true;
        }

        if (action.equalsIgnoreCase("add")) {
            for (List<Player> team: customTeams.values()) {
                if (team.contains(player)) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(langHandler.getMessage(null, "team.no-player"));
                    } else {
                        sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-player"));
                    }
                    return true;
                }
            }
            customTeams.computeIfAbsent(teamName, k -> new ArrayList<>()).add(player);
            if (!(sender instanceof Player)) {
                sender.sendMessage(langHandler.getMessage(null, "team.added", player.getName(), teamName));
            } else {
                sender.sendMessage(langHandler.getMessage((Player) sender, "team.added", player.getName(), teamName));
            }
        } else if (action.equalsIgnoreCase("remove")) {
            List<Player> team = customTeams.get(teamName);
            if (team != null && team.contains(player)) {
                team.remove(player);
                if (!(sender instanceof Player)) {
                    sender.sendMessage(langHandler.getMessage(null, "team.removed", player.getName(), teamName));
                } else {
                    sender.sendMessage(langHandler.getMessage((Player) sender, "team.removed", player.getName(), teamName));
                }
            } else {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(langHandler.getMessage(null, "team.no-removed", player.getName(), teamName));
                } else {
                    sender.sendMessage(langHandler.getMessage((Player) sender, "team.no-removed", player.getName(), teamName));
                }
            }
        }

        return true;
    }
}