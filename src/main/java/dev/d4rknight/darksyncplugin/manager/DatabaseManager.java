package dev.d4rknight.darksyncplugin.manager;

import dev.d4rknight.darksyncplugin.DarkSyncPlugin;
import dev.d4rknight.darksyncplugin.serializer.InventorySerializer;
import dev.d4rknight.darksyncplugin.utils.ChecksumUtils;
import dev.d4rknight.darksyncplugin.utils.SyncLogger;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

  private final DarkSyncPlugin plugin;
  private Connection connection;

  public DatabaseManager(DarkSyncPlugin plugin) {
    this.plugin = plugin;
    connect();
    createTable();
  }

  private void connect() {
    try {
      String url = "jdbc:mysql://" +
              plugin.getConfig().getString("mysql.host") + ":" +
              plugin.getConfig().getInt("mysql.port") + "/" +
              plugin.getConfig().getString("mysql.database") +
              "?useSSL=false&autoReconnect=true";

      connection = DriverManager.getConnection(
              url,
              plugin.getConfig().getString("mysql.user"),
              plugin.getConfig().getString("mysql.password")
      );

      SyncLogger.success("Connected to MySQL.");
    } catch (SQLException e) {
      SyncLogger.error("MySQL connection failed: " + e.getMessage());
    }
  }

  private void createTable() {
    try (PreparedStatement ps = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS player_inventories (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "inventory LONGBLOB NOT NULL," +
                    "checksum VARCHAR(64) NOT NULL)"
    )) {
      ps.executeUpdate();
    } catch (SQLException e) {
      SyncLogger.error("Failed to create table: " + e.getMessage());
    }
  }

  public CompletableFuture<ItemStack[]> loadInventory(UUID uuid) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        byte[] cached = RedisManager.loadInventoryFromRedis(uuid);
        if (cached != null) {
          SyncLogger.debug("Loaded inventory from Redis for " + uuid);
          return InventorySerializer.deserialize(cached);
        }
      } catch (Exception e) {
        SyncLogger.error("Failed to load inventory from Redis for " + uuid + ": " + e.getMessage());
      }

      // Redis miss -> fallback to MySQL
      try (PreparedStatement ps = connection.prepareStatement(
              "SELECT inventory FROM player_inventories WHERE uuid = ?")) {
        ps.setString(1, uuid.toString());

        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            byte[] data = rs.getBytes("inventory");

            RedisManager.saveInventoryToRedis(uuid, data);
            SyncLogger.debug("Loaded inventory from MySQL and re-cached to Redis for " + uuid);

            return InventorySerializer.deserialize(data);
          }
        }
      } catch (SQLException e) {
        SyncLogger.error("Error loading inventory from MySQL for " + uuid + ": " + e.getMessage());
      }

      return null;
    });
  }

  public void saveInventory(UUID uuid, ItemStack[] contents) {
    CompletableFuture.runAsync(() -> {
      try {
        byte[] data = InventorySerializer.serialize(contents);
        String checksum = ChecksumUtils.hash(data);

        RedisManager.saveInventoryToRedis(uuid, data);
        SyncLogger.debug("Cached inventory to Redis for " + uuid);

        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO player_inventories (uuid, inventory, checksum) VALUES (?, ?, ?)")) {
          ps.setString(1, uuid.toString());
          ps.setBytes(2, data);
          ps.setString(3, checksum);
          ps.executeUpdate();

          SyncLogger.debug("Inventory saved to MySQL for " + uuid);
        }

      } catch (Exception e) {
        SyncLogger.error("Error saving inventory for " + uuid + ": " + e.getMessage());
      }
    });
  }
}
