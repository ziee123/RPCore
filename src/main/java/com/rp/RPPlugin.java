package com.rp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RPPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("RP-Core aktif!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rp")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage("Hanya player yang bisa pakai /rp");
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + "Pakai: /rp <pesan>");
            return true;
        }

        String message = String.join(" ", args);
        String format = getConfig().getString("rp-format", "&7[RP] &e%player%&7: &f%message%");
        int distance = getConfig().getInt("rp-distance", 15);

        String finalMsg = ChatColor.translateAlternateColorCodes('&', 
            format.replace("%player%", p.getName()).replace("%message%", message));

        int count = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getWorld() != p.getWorld()) continue;
            if (target.getLocation().distance(p.getLocation()) <= distance) {
                target.sendMessage(finalMsg);
                count++;
            }
        }
        
        if (count == 0) p.sendMessage(finalMsg);

        return true;
    }
}
