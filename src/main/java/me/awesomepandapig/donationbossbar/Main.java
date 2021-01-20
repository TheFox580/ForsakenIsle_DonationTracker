package me.awesomepandapig.donationbossbar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONException;
import java.io.IOException;

public final class Main extends JavaPlugin implements Listener {

    public Bar bar;
    String token = this.getConfig().getString("access-token");
    String id = this.getConfig().getString("campaign-id");
    String mainTitleColor = this.getConfig().getString("main-title-color");
    String mainBarColor = this.getConfig().getString("main-bar-color");
    String goalMsg = this.getConfig().getString("goal-message");
    String goalTitleColor = this.getConfig().getString("goal-title-color:");
    String goalBarColor = this.getConfig().getString("goal-bar-color:");

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        bar = new Bar(this);
        try {
            bar.createBar(token, id, mainTitleColor, mainBarColor, goalMsg, goalTitleColor, goalBarColor);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (Player on: Bukkit.getOnlinePlayers()) {
                bar.addPlayer(on);
            }
        }
    }

    @Override
    public void onDisable() {
        bar.getBar().removeAll();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!bar.getBar().getPlayers().contains(event.getPlayer())) {
            bar.addPlayer(event.getPlayer());
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("donationbb")) {
            if (!sender.hasPermission("donationbb.reload")) {
                sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command" +
                        ". Please contact the server administrators if you believe that this is in error.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2[&aDonation Bossbar&2]\n&aVersion:&7 1.0\n&aDeveloper:&7 awesomepandapig\n&aCommands:&7 /donationbb reload"));
                return true;
            }
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReloaded Donation Bossbar&r"));
                this.reloadConfig();
                bar.getBar().removeAll();
                String token = this.getConfig().getString("access-token");
                String id = this.getConfig().getString("campaign-id");
                String mainTitleColor = this.getConfig().getString("main-title-color");
                String mainBarColor = this.getConfig().getString("main-bar-color");
                String goalMsg = this.getConfig().getString("goal-message");
                String goalTitleColor = this.getConfig().getString("goal-title-color:");
                String goalBarColor = this.getConfig().getString("goal-bar-color:");
                Bukkit.getLogger().info(id);
                bar = new Bar(this);
                try {
                    bar.createBar(token, id, mainTitleColor, mainBarColor, goalMsg, goalTitleColor, goalBarColor);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (Bukkit.getOnlinePlayers().size() > 0) {
                    for (Player on: Bukkit.getOnlinePlayers()) {
                        bar.addPlayer(on);
                    }
                }
            }
        }
        return false;
    }
}
