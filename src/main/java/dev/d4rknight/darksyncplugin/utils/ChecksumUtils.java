package dev.d4rknight.darksyncplugin.utils;

import java.security.MessageDigest;

public class ChecksumUtils {
  public static String hash(byte[] data) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(data);
      StringBuilder sb = new StringBuilder();
      for (byte b : digest)
        sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Checksum failure", e);
    }
  }
}
