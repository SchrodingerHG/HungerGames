package me.aymanisam.hungergames.listeners;

import me.aymanisam.hungergames.HungerGames;
import me.aymanisam.hungergames.handlers.ConfigHandler;
import me.aymanisam.hungergames.handlers.LangHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class EntityListener implements Listener {
    private final HungerGames plugin;
    private final LangHandler langHandler;
    private final ConfigHandler configHandler;
    private final Random random = new Random();
    HashSet<EntityType> allowedMobs = new HashSet<>();

    public EntityListener(HungerGames plugin, LangHandler langHandler) {
        this.plugin = plugin;
        this.langHandler = langHandler;
        this.configHandler = plugin.getConfigHandler();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();
        FileConfiguration worldConfig = configHandler.getWorldConfig(world);
        boolean mobCandyDrop = worldConfig.getBoolean("candydrop.mob");
        boolean playerCandyDrop = worldConfig.getBoolean("candydrop.player");

        if (!mobCandyDrop && !playerCandyDrop) {
            return;
        }

        boolean gameStarted = HungerGames.gameStarted.getOrDefault(world, false);

        if (!gameStarted) {
            return;
        }

        Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        if (mobCandyDrop && entity.getType() != EntityType.PLAYER) {
            if (allowedMobs.isEmpty()) {
                List<String> mobList = worldConfig.getStringList("candydrop.mobs");

                if (mobList.isEmpty()) {
                    return;
                }

                for (String mobName : mobList) {
                    try {
                        EntityType entityType = EntityType.valueOf(mobName.toUpperCase());
                        allowedMobs.add(entityType);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().log(Level.WARNING, "Invalid mob type in config: " + mobName);
                    }
                }
            }

            if (!allowedMobs.contains(entity.getType())) {
                return;
            }
        }

        int maxDrop =  worldConfig.getInt("candydrop.max-drop", 3);
        int minDrop =  worldConfig.getInt("candydrop.min-drop", 1);
        int dropRange = worldConfig.getInt("candydrop.drop-range", 1);

        int dropAmount = minDrop + random.nextInt((maxDrop - minDrop) + 1);
        Location deathLocation = entity.getLocation();
        HashSet<String> usedLocations = new HashSet<>();

        for (int i = 0; i < dropAmount; i++) {
            ItemStack candy = new ItemStack(Material.SUGAR, 1);
            Location dropLocation = findAvailableDropLocation(deathLocation, dropRange, usedLocations);

            if (dropLocation != null) {
                world.dropItemNaturally(dropLocation, candy);
                usedLocations.add(blockKey(dropLocation));
            }
        }
    }

    private Location findAvailableDropLocation(Location center, int range, HashSet<String> usedLocations) {
        int maxTries = 10;
        for (int tries = 0; tries < maxTries; tries++) {
            int offsetX = random.nextInt(range * 2 + 1) - range;
            int offsetZ = random.nextInt(range * 2 + 1) - range;

            Location potentialLocation = center.clone().add(offsetX, 0.1, offsetZ);
            String locationKey = blockKey(potentialLocation);

            if (!usedLocations.contains(locationKey)) {
                return potentialLocation;
            }
        }

        return center;
    }

    private String blockKey(Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        Item item1 = event.getEntity();
        Item item2 = event.getTarget();
        World world = item1.getWorld();
        FileConfiguration worldConfig = configHandler.getWorldConfig(world);

        boolean itemMerge = worldConfig.getBoolean("candydrop.merge-items");

        if (!itemMerge) {
            if (item1.getItemStack().getType() == Material.SUGAR && item2.getItemStack().getType() == Material.SUGAR) {
                event.setCancelled(true);
            }
        }
    }
}
