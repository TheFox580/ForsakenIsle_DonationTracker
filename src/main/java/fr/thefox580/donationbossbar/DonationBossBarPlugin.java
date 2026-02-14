package fr.thefox580.donationbossbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static fr.thefox580.donationbossbar.Colors.colorizeComponent;

public final class DonationBossBarPlugin extends JavaPlugin {

    private DonationBar donationBar;
    private GoalEvents goalEvents;
    private TiltifyData tiltifyData;

    public static DonationBossBarPlugin getInstance() {
        return DonationBossBarPlugin.getPlugin(DonationBossBarPlugin.class);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        tiltifyData = new TiltifyData(); // fetches for token every 1h59, before the token becomes invalid
        this.createBossBar(); // create the boss bar before registering the event listener
        new RegisterPlayer(this, donationBar); // registers players to check who to give rewards to
    }

    @Override
    public void onDisable() {
        if (this.donationBar == null) {
            return; // if donation bar is null, then it hasn't been created yet
        }
        if (this.donationBar.getBossBar() != null){
            this.donationBar.getBossBar().removeAll();
        }
    }

    public void createBossBar() {
        this.reloadConfig();

        if (this.donationBar == null) { // if null then create a new instance
            this.donationBar = new DonationBar();
            this.goalEvents = new GoalEvents(this);
        }

        // remove all players from the boss bar and cancel the task
        BossBar bossBar = this.donationBar.getBossBar();
        if (bossBar != null) bossBar.removeAll(); // if the boss bar is null, then it hasn't been created yet
        this.donationBar.attemptToCancel();

        // get the config values
        String id = this.getConfig().getString("campaign-id");
        String mainTitleColor = this.getConfig().getString("main-title-color");
        String mainBarColor = this.getConfig().getString("main-bar-color");
        String goalMsg = this.getConfig().getString("goal-message");
        String goalTitleColor = this.getConfig().getString("goal-title-color");
        String goalBarColor = this.getConfig().getString("goal-bar-color");

        // create the boss bar
        this.donationBar = new DonationBar();
        this.donationBar.createBar(id, mainTitleColor, mainBarColor, goalMsg, goalTitleColor, goalBarColor);

        // add all online players to the boss bar
        Bukkit.getOnlinePlayers().forEach(this.donationBar::addPlayer);
    }

    /**
     * The donation boss bar command.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("donationbb")) {
            if (!sender.hasPermission("donationbb")) {
                sender.sendMessage(Component.text("I'm sorry, but you do not have permission to perform this command" +
                        ". Please contact the server administrators if you believe that this is in error.", NamedTextColor.RED));
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(colorizeComponent("&2[&aDonation Bossbar&2]\n&aVersion:&7 4.0.0\n&aDeveloper:&7 awesomepandapig, TheFox580\n&aCommands:&7 /donationbb reload, /donationbb donations <int>"));
                return true;
            } else {
                if (args[0].equalsIgnoreCase("reload")) {
                this.reloadConfig();
                sender.sendMessage(colorizeComponent("&aReloaded Donation Bossbar&r"));
                this.createBossBar();
            } else if (args[0].equalsIgnoreCase("donations")){
                    try {
                        goalEvents.checkDonations(Integer.parseInt(args[1]));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


        }
        return false;
    }

    public TiltifyData getTiltifyData() {
        return tiltifyData;
    }
}
