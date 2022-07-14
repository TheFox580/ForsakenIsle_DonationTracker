package me.awesomepandapig.donationbossbar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static me.awesomepandapig.donationbossbar.Colors.colorize;

public final class DonationBossBarPlugin extends JavaPlugin {

    private DonationBar donationBar;

    public static DonationBossBarPlugin getInstance() {
        return DonationBossBarPlugin.getPlugin(DonationBossBarPlugin.class);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                if (!donationBar.getBossBar().getPlayers().contains(event.getPlayer())) {
                    donationBar.addPlayer(event.getPlayer());
                }
            }
        }, this);
        this.createBossBar();
    }

    @Override
    public void onDisable() {
        this.donationBar.getBossBar().removeAll();
    }

    public void createBossBar() {
        this.reloadConfig();
        donationBar.getBossBar().removeAll();
        donationBar.attemptToCancel();

        String token = this.getConfig().getString("access-token");
        String id = this.getConfig().getString("campaign-id");
        String mainTitleColor = this.getConfig().getString("main-title-color");
        String mainBarColor = this.getConfig().getString("main-bar-color");
        String goalMsg = this.getConfig().getString("goal-message");
        String goalTitleColor = this.getConfig().getString("goal-title-color");
        String goalBarColor = this.getConfig().getString("goal-bar-color");

        donationBar = new DonationBar();
        donationBar.createBar(token, id, mainTitleColor, mainBarColor, goalMsg, goalTitleColor, goalBarColor);
        Bukkit.getOnlinePlayers().forEach(donationBar::addPlayer);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("donationbb")) {
            if (!sender.hasPermission("donationbb")) {
                sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command" +
                        ". Please contact the server administrators if you believe that this is in error.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(colorize("&2[&aDonation Bossbar&2]\n&aVersion:&7 1.0\n&aDeveloper:&7 awesomepandapig\n&aCommands:&7 /donationbb reload"));
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(colorize("&aReloaded Donation Bossbar&r"));
                createBossBar();
            }
        }
        return false;
    }
}
