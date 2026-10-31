package com.rp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RPPlugin extends JavaPlugin implements Listener {

    private Set<UUID> lockedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("RP-Core TOGGLE mode aktif!");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        // Kalo lagi mode RP / RPLAYER, chat nya jadi RP local otomatis, bukan global
        if (lockedPlayers.contains(p.getUniqueId())) {
            e.setCancelled(true);
            String format = getConfig().getString("rp-format", "&7[MC] &e%player%&7: &f%message%")
                .replace("%player%", p.getName()).replace("%message%", e.getMessage());
            sendLocal(p, format, getConfig().getInt("rp-distance", 15));
        } else {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "Chat biasa mati. /rp | /rplayer | /ooc");
        }
    }

    @EventHandler
    public void onCommandBlock(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage().toLowerCase();
        if (msg.startsWith("/rp") || msg.startsWith("/rplayer") || msg.startsWith("/ooc") || msg.startsWith("/login") || msg.startsWith("/register")) return;
        if (lockedPlayers.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.GRAY + "Mode RP aktif! Fitur terkunci. /ooc buat balik.");
        }
    }

    @EventHandler
    public void onTab(PlayerCommandSendEvent e) {
        e.getCommands().clear();
        e.getCommands().add("rp");
        e.getCommands().add("rplayer");
        e.getCommands().add("ooc");
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("rp")) {
            if (!p.hasPermission("rp.mc")) {
                p.sendMessage(ChatColor.RED + "/rp khusus MC! Kamu pakai /rplayer");
                return true;
            }
            lockedPlayers.add(p.getUniqueId());
            p.sendMessage(ChatColor.GREEN + ">> MODE MC AKTIF << Fitur terkunci, chat hidden. Chat kamu sekarang jadi RP local. /ooc buat balik.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("rplayer")) {
            lockedPlayers.add(p.getUniqueId());
            p.sendMessage(ChatColor.GREEN + ">> MODE PLAYER AKTIF << Fitur terkunci, chat hidden. Chat kamu sekarang jadi RP local. /ooc buat balik.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("ooc")) {
            if (!p.hasPermission("rp.ooc")) {
                p.sendMessage(ChatColor.RED + "No akses OOC. Set LuckPerms rp.ooc");
                return true;
            }
            lockedPlayers.remove(p.getUniqueId());
            p.sendMessage(ChatColor.YELLOW + ">> MODE OOC (DEFAULT) << Balik normal. Fitur kebuka.");
            if (args.length > 0) {
                String msg = String.join(" ", args);
                String format = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ooc-format", "&8[OOC] &f%player%: &f%message%").replace("%player%", p.getName()).replace("%message%", msg));
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (lockedPlayers.contains(online.getUniqueId())) continue;
                    online.sendMessage(format);
                }
            }
            return true;
        }
        return true;
    }

    private void sendLocal(Player sender, String formatted, int distance) {
        String colored = ChatColor.translateAlternateColorCodes('&', formatted);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getWorld() != sender.getWorld()) continue;
            if (online.getLocation().distance(sender.getLocation()) > distance) continue;
            if (lockedPlayers.contains(online.getUniqueId()) || online.equals(sender)) {
                 online.sendMessage(colored);
            }
        }
    }
}
