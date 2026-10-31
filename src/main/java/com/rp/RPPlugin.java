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
        getLogger().info("RPlugin (rplugin.*) aktif!");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (lockedPlayers.contains(p.getUniqueId())) {
            e.setCancelled(true);
            String format = getConfig().getString("rp-format", "&7[RP] &e%player%&7: &f%message%")
                .replace("%player%", p.getName()).replace("%message%", e.getMessage());
            sendLocal(p, format, getConfig().getInt("rp-distance", 15));
        } else {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "Chat biasa mati. Pakai /rp | /rplayer | /ooc");
        }
    }

    @EventHandler
    public void onCommandBlock(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage().toLowerCase();
        if (msg.startsWith("/rp") || msg.startsWith("/rplayer") || msg.startsWith("/ooc") || msg.startsWith("/login") || msg.startsWith("/register")) return;
        if (lockedPlayers.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.GRAY + "Mode RP aktif! Fitur terkunci. /ooc buat balik default.");
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
            if (!p.hasPermission("rplugin.rp")) {
                p.sendMessage(ChatColor.RED + "Butuh rplugin.rp | /lp user " + p.getName() + " permission set rplugin.rp true");
                return true;
            }
            lockedPlayers.add(p.getUniqueId());
            p.sendMessage(ChatColor.GREEN + ">> MODE MC (rplugin.rp) AKTIF <<");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("rplayer")) {
            if (!p.hasPermission("rplugin.rplayer")) {
                p.sendMessage(ChatColor.RED + "Butuh rplugin.rplayer");
                return true;
            }
            lockedPlayers.add(p.getUniqueId());
            p.sendMessage(ChatColor.GREEN + ">> MODE PLAYER (rplugin.rplayer) AKTIF <<");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("ooc")) {
            if (!p.hasPermission("rplugin.ooc")) {
                p.sendMessage(ChatColor.RED + "Butuh rplugin.ooc");
                return true;
            }
            lockedPlayers.remove(p.getUniqueId());
            if (args.length == 0) {
                p.sendMessage(ChatColor.YELLOW + ">> MODE OOC (rplugin.ooc) BALIK DEFAULT <<");
                return true;
            }
            String msg = String.join(" ", args);
            String format = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ooc-format", "&8[OOC] &f%player%: &f%message%").replace("%player%", p.getName()).replace("%message%", msg));
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (lockedPlayers.contains(online.getUniqueId())) continue;
                online.sendMessage(format);
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
            if (lockedPlayers.contains(online.getUniqueId())) {
                 online.sendMessage(colored);
            }
        }
        Bukkit.getConsoleSender().sendMessage(colored);
    }
}
