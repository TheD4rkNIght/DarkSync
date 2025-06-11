package dev.d4rknight.darksyncplugin.serializer;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;

public class InventorySerializer {

  public static byte[] serialize(ItemStack[] contents) {
    if (contents == null) return new byte[0];
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {

      oos.writeObject(contents);
      return baos.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to serialize inventory: " + e.getMessage(), e);
    }
  }

  public static ItemStack[] deserialize(byte[] data) {
    if (data == null || data.length == 0) return new ItemStack[0];
    try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
         BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {

      Object obj = ois.readObject();
      if (obj instanceof ItemStack[] contents) {
        return contents;
      } else {
        throw new IOException("Deserialized object is not ItemStack[]");
      }

    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Failed to deserialize inventory: " + e.getMessage(), e);
    }
  }
}
