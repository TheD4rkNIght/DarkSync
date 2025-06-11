# DarkSync

DarkSync is a backend Minecraft plugin that syncs player inventories across servers using MySQL. It’s fast, handles armor/offhand, avoids dupes, and works for large playerbases.

Still in beta. I’m learning Java. Contributions and suggestions welcome.

---

## Features

- Syncs full inventory (main, hotbar, armor, offhand)
- Auto saves on quit, loads on join
- MySQL-based (async, no thread blocking)
- Seamless sync using Redis, caches player data when switching servers to reduce DB calls and make syncs near-instant
- Anti-dupe system using locks + checksum
- Auto table creation
- Toggleable logs: DEBUG / INFO / ERROR

---

## Setup

1. Drop the plugin on each backend server
2. Make sure all servers use the same MySQL database
3. Start the server, tables are created automatically
4. Inventory will sync when players switch servers

---

## Todo

- [ ] Ender chest sync  
- [ ] XP + level sync  
- [ ] Advancements  
- [ ] Potion effects  
- [ ] Health, hunger, etc  
- [ ] Admin command (`/darksync`)  
- [ ] Redis or caching  
- [ ] Velocity plugin messaging  
- [ ] Backup/rollback support

---

## Notes

- No Velocity support yet (data loads on join)
- No commands or GUI yet
- Works only on backend servers, not proxy

---

## Contributing / Testing

I'm still learning Java and building this as I go.  
If you're good with plugins, databases, or just want to test, feel free to help out.  
Bug reports, ideas, and pull requests are always welcome.

---

## License

GNU General Public License v3.0
