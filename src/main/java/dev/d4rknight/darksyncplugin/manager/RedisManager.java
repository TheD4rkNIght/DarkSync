package dev.d4rknight.darksyncplugin.manager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Base64;
import java.util.UUID;

public class RedisManager {

  private static JedisPool pool;

  public static void init(String host, int port, String password) {
    JedisPoolConfig config = new JedisPoolConfig();
    pool = new JedisPool(config, host, port, 2000, password);
  }

  public static boolean isEnabled() {
    return pool != null && !pool.isClosed();
  }

  public static void saveInventoryToRedis(UUID uuid, byte[] data) {
    if (!isEnabled()) return;

    try (Jedis jedis = pool.getResource()) {
      String key = "inv:" + uuid.toString();
      String encoded = Base64.getEncoder().encodeToString(data);
      jedis.setex(key, 60, encoded);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static byte[] loadInventoryFromRedis(UUID uuid) {
    if (!isEnabled()) return null;

    try (Jedis jedis = pool.getResource()) {
      String key = "inv:" + uuid.toString();
      String encoded = jedis.get(key);
      if (encoded != null) {
        jedis.del(key);
        return Base64.getDecoder().decode(encoded);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void set(String key, byte[] value, int ttl) {
    if (!isEnabled()) return;

    try (Jedis jedis = pool.getResource()) {
      jedis.setex(key.getBytes(), ttl, value);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static byte[] get(String key) {
    if (!isEnabled()) return null;

    try (Jedis jedis = pool.getResource()) {
      return jedis.get(key.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void shutdown() {
    if (pool != null && !pool.isClosed()) {
      pool.close();
    }
  }
}
