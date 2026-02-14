package fr.thefox580.donationbossbar;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.Locale;

import static fr.thefox580.donationbossbar.Colors.colorize;

public final class DonationBar {
    private BossBar bar;
    private BukkitTask bukkitTask;
    private final GoalEvents goalEvents;
    private final DonationBossBarPlugin plugin = DonationBossBarPlugin.getInstance();
    static FileConfiguration config = DonationBossBarPlugin.getInstance().getConfig();

    public DonationBar() {
        this.goalEvents = new GoalEvents(plugin);
    }

    public void createBar(String id, String mainTitleColor, String mainBarColor, String goalMsg, String goalTitleColor, String goalBarColor) {
        this.bar = Bukkit.createBossBar(colorize("&dWelcome to Forsaken Isle Season 3"), BarColor.PINK, BarStyle.SOLID);
        this.bar.setVisible(true);

        this.bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(DonationBossBarPlugin.getInstance(), () -> {
            try {
                final JsonObject jsonObject = plugin.getTiltifyData().requestJson(new URI("https://v5api.tiltify.com/api/public/team_campaigns/" + id).toURL());
                Bukkit.getScheduler().runTask(DonationBossBarPlugin.getInstance(), () -> {
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    double totalAmountRaised = data.get("amount_raised").getAsJsonObject().get("value").getAsDouble();
                    double fundraiserGoalAmount = data.get("goal").getAsJsonObject().get("value").getAsDouble();
                    double progress = totalAmountRaised / fundraiserGoalAmount;
                    if (progress >= 1) {
                        // the goal has been reached
                        this.bar.setProgress(1);
                        this.bar.setColor(BarColor.valueOf(goalBarColor));
                        this.bar.setTitle(colorize(goalTitleColor + goalMsg));
                    } else {
                        this.bar.setProgress(progress);
                        this.bar.setColor(BarColor.valueOf(mainBarColor));
                        this.bar.setTitle(colorize(mainTitleColor + "Raised £" + this.formatNumber(totalAmountRaised) + " of £" + this.formatNumber(fundraiserGoalAmount)));
                    }

                    // Check goals based on the donations that were done
                    if (config.getDouble("total_amount_raised") < totalAmountRaised){
                        double diff = totalAmountRaised - config.getDouble("total_amount_raised");
                        diff = (double) Math.round(diff * 100) / 100;
                        plugin.getLogger().warning("We've raised £" + diff + " more!");
                        config.set("total_amount_raised", totalAmountRaised);
                        plugin.saveConfig();
                        try {
                            goalEvents.checkDonations(diff);
                        } catch (IOException | URISyntaxException e) {
                            plugin.getLogger().warning(e.toString());
                        }
                        //goalEvents.checkGoals(diff);
                    }
                });
            } catch (IOException | URISyntaxException e) {
                plugin.getLogger().warning(e.toString());
            }
        }, 0L, 10 * 20L);
    }

    private String formatNumber(double number) {
        return NumberFormat.getInstance(Locale.US).format(number);
    }

    public void addPlayer(Player player) {
        this.bar.addPlayer(player);
    }

    public @Nullable BossBar getBossBar() {
        return this.bar;
    }

    public void attemptToCancel() {
        if (this.bukkitTask == null) return;
        if (this.bukkitTask.isCancelled()) return;
        this.bukkitTask.cancel();
    }
}
