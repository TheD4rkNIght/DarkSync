package dev.d4rknight.darksyncplugin.utils;

import dev.d4rknight.darksyncplugin.DarkSyncPlugin;

public class SyncLogger {

  private static final String level = DarkSyncPlugin.getInstance()
          .getConfig().getString("logging.level", "INFO").toUpperCase();

// ANSI
  private static final String RESET = "\u001B[0m";
  private static final String GRAY = "\u001B[37m";
  private static final String AQUA = "\u001B[36m";
  private static final String GREEN = "\u001B[32m";
  private static final String RED = "\u001B[31m";

  public static void debug(String msg) {
    if (level.equals("DEBUG")) log(GRAY, "[DEBUG] ", msg);
  }

  public static void info(String msg) {
    if (level.equals("DEBUG") || level.equals("INFO"))
      log(AQUA, "[INFO] ", msg);
  }

  public static void success(String msg) {
    if (!level.equals("ERROR")) log(GREEN, "[OK] ", msg);
  }

  public static void error(String msg) {
    log(RED, "[ERROR] ", msg);
  }

  private static void log(String color, String prefix, String msg) {
    DarkSyncPlugin.getInstance().getLogger().info(color + prefix + RESET + msg);
  }
}
