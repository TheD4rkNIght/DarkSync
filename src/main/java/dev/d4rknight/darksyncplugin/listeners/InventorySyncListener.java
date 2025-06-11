package dev.d4rknight.darksyncplugin.listeners;

import dev.d4rknight.darksyncplugin.DarkSyncPlugin;
import dev.d4rknight.darksyncplugin.utils.SyncLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventorySyncListener implements Listener {

  private final DarkSyncPlugin plugin;
  private final Set<UUID> locked = ConcurrentHashMap.newKeySet();

  public InventorySyncListener(DarkSyncPlugin plugin) {
    this.plugin = plugin;
  }

  private void lock(Player player) {
    locked.add(player.getUniqueId());
    player.sendMessage(ChatColor.RED + "Your inventory is syncing...");
  }

  private void unlock(Player player) {
    locked.remove(player.getUniqueId());
    player.sendMessage(ChatColor.GREEN + "Inventory sync complete.");
  }

  private boolean isLocked(Player player) {
    return locked.contains(player.getUniqueId());
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();

    player.getInventory().clear();
    lock(player);

    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
      if (isLocked(player)) {
        unlock(player);
        SyncLogger.error("Fallback unlock triggered for " + player.getName());
      }
    }, 60L);

    plugin.getDatabase().loadInventory(uuid).thenAccept(contents -> {
      Bukkit.getScheduler().runTask(plugin, () -> {
        if (!player.isOnline()) return;

        if (contents != null) {
          player.getInventory().setContents(contents);
          SyncLogger.debug("Inventory loaded and applied for " + player.getName());
        } else {
          SyncLogger.debug("No inventory data found for " + player.getName());
        }

        unlock(player);
      });
    });
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();
    ItemStack[] contents = player.getInventory().getContents();

    plugin.getDatabase().saveInventory(uuid, contents);
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    if (isLocked(event.getPlayer())) event.setCancelled(true);
  }

  @EventHandler
  public void onPickup(EntityPickupItemEvent event) {
    if (event.getEntity() instanceof Player player && isLocked(player)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player && isLocked(player)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onMoveItem(InventoryDragEvent event) {
    if (event.getWhoClicked() instanceof Player player && isLocked(player)) {
      event.setCancelled(true);
    }
  }
}
