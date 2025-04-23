package dev.nighter.teaSafeDrop;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Accessors(chain = false)
public final class TeaSafeDrop extends JavaPlugin {
    @Getter
    private static TeaSafeDrop instance;

    private boolean defaultProtectionEnabled;
    private final Map<String, Boolean> worldProtectionStatus = new ConcurrentHashMap<>();
    private final Map<Material, Boolean> globalProtectedItems = new ConcurrentHashMap<>();
    private final Map<String, Map<Material, Boolean>> worldSpecificProtectedItems = new ConcurrentHashMap<>();
    private boolean debug;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Load configuration
        loadConfig();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);

        // Register commands
        PluginCommand command = getCommand("teasafedrop");
        if (command != null) {
            CommandHandler commandHandler = new CommandHandler(this);
            command.setExecutor(commandHandler);
            command.setTabCompleter(commandHandler);
        }

        getLogger().info("TeaSafeDrop has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clear all maps to prevent memory leaks
        worldProtectionStatus.clear();
        globalProtectedItems.clear();
        worldSpecificProtectedItems.clear();

        getLogger().info("TeaSafeDrop has been disabled!");
    }

    /**
     * Loads or reloads the configuration
     */
    public void loadConfig() {
        // Reload config from disk
        reloadConfig();
        FileConfiguration config = getConfig();

        // Clear existing maps
        worldProtectionStatus.clear();
        globalProtectedItems.clear();
        worldSpecificProtectedItems.clear();

        // Load global settings
        defaultProtectionEnabled = config.getBoolean("settings.default-protection-enabled", true);
        debug = config.getBoolean("settings.debug", false);

        // Load global protected items
        ConfigurationSection itemsSection = config.getConfigurationSection("protected-items");
        if (itemsSection != null) {
            for (String itemName : itemsSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(itemName.toUpperCase());
                    boolean isProtected = itemsSection.getBoolean(itemName);
                    globalProtectedItems.put(material, isProtected);

                    if (debug) {
                        getLogger().info("Loaded global item protection: " + itemName + " = " + isProtected);
                    }
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid material in config: " + itemName);
                }
            }
        }

        // Load per-world configurations
        ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
        if (worldsSection != null) {
            for (String worldName : worldsSection.getKeys(false)) {
                boolean worldProtected = worldsSection.getBoolean(worldName + ".protection-enabled", defaultProtectionEnabled);
                worldProtectionStatus.put(worldName, worldProtected);

                // Load world-specific protected items if they exist
                ConfigurationSection worldItemsSection = worldsSection.getConfigurationSection(worldName + ".protected-items");
                if (worldItemsSection != null) {
                    Map<Material, Boolean> worldItems = new HashMap<>();
                    for (String itemName : worldItemsSection.getKeys(false)) {
                        try {
                            Material material = Material.valueOf(itemName.toUpperCase());
                            boolean isProtected = worldItemsSection.getBoolean(itemName);
                            worldItems.put(material, isProtected);

                            if (debug) {
                                getLogger().info("Loaded world-specific item protection for " + worldName + ": " + itemName + " = " + isProtected);
                            }
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("Invalid material in world config: " + worldName + "." + itemName);
                        }
                    }
                    if (!worldItems.isEmpty()) {
                        worldSpecificProtectedItems.put(worldName, worldItems);
                    }
                }

                if (debug) {
                    getLogger().info("Loaded world protection for " + worldName + " = " + worldProtected);
                }
            }
        }

        getLogger().info("Configuration loaded: " + globalProtectedItems.size() + " protected items, " +
                worldProtectionStatus.size() + " configured worlds.");
    }

    /**
     * Checks if an item should be protected in a specific world
     *
     * @param material The material to check
     * @param world The world to check in
     * @return true if the item should be protected, false otherwise
     */
    public boolean isItemProtected(Material material, World world) {
        if (world == null || material == null) {
            return false;
        }

        String worldName = world.getName();

        // Check if protection is enabled for this world
        boolean worldProtectionEnabled = worldProtectionStatus.getOrDefault(worldName, defaultProtectionEnabled);
        if (!worldProtectionEnabled) {
            return false;
        }

        // Check for world-specific item protection
        Map<Material, Boolean> worldItems = worldSpecificProtectedItems.get(worldName);
        if (worldItems != null && worldItems.containsKey(material)) {
            return worldItems.get(material);
        }

        // Fall back to global item protection
        return globalProtectedItems.getOrDefault(material, false);
    }
}