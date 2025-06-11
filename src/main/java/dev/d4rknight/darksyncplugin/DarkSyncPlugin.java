package dev.d4rknight.darksyncplugin;

import dev.d4rknight.darksyncplugin.listeners.InventorySyncListener;
import dev.d4rknight.darksyncplugin.manager.DatabaseManager;
import dev.d4rknight.darksyncplugin.manager.RedisManager;
import dev.d4rknight.darksyncplugin.utils.SyncLogger;
import org.bukkit.plugin.java.JavaPlugin;

public class DarkSyncPlugin extends JavaPlugin {

    private static DarkSyncPlugin instance;
    private DatabaseManager database;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        SyncLogger.info("Loading configuration...");

        try {
            this.database = new DatabaseManager(this);
        } catch (Exception e) {
            SyncLogger.error("Failed to initialize MySQL: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getConfig().getBoolean("redis.enabled", false)) {
            String host = getConfig().getString("redis.host");
            int port = getConfig().getInt("redis.port");
            String password = getConfig().getString("redis.password");            try {
                RedisManager.init(host, port, password);
                SyncLogger.info("Redis caching enabled.");
            } catch (Exception e) {
                SyncLogger.error("Redis failed to start: " + e.getMessage());
            }
        }

        getServer().getPluginManager().registerEvents(new InventorySyncListener(this), this);

        SyncLogger.success("DarkSync enabled. Inventory sync is live.");
    }

    @Override
    public void onDisable() {
        SyncLogger.info("DarkSync shutting down...");
        RedisManager.shutdown();
    }

    public static DarkSyncPlugin getInstance() {
        return instance;
    }

    public DatabaseManager getDatabase() {
        return database;
    }
}
