package me.awesomepandapig.donationbossbar;

import com.google.common.net.HttpHeaders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

public class Bar {

    private int taskID = -1;
    private final Main plugin;
    private BossBar bar;

    public Bar(Main plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player) {
        bar.addPlayer(player);
    }

    public BossBar getBar() {
        return bar;
    }

    public void createBar(String token, String id, String mainTitleColor, String mainBarColor, String goalMsg,
                          String goalTitleColor, String goalBarColor) throws IOException, JSONException {
        bar = Bukkit.createBossBar(format("&cWelcome to Donation Bossbar"), BarColor.PINK,
                BarStyle.SOLID);
        bar.setVisible(true);
        cast(token, id, mainTitleColor, mainBarColor, goalMsg, goalTitleColor, goalBarColor);
    }

    public void cast(String token, String id, String mainTitleColor, String mainBarColor, String goalMsg,
                     String goalTitleColor, String goalBarColor) throws IOException, JSONException {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            int count = 0;
            double amt = readJson() [0];
            double goal = readJson() [1];

            BigDecimal donatedAmt = new BigDecimal(String.valueOf(amt));
            BigDecimal donationGoal = new BigDecimal(String.valueOf(goal));

            double progress = amt/goal;

            public Double[] readJson() throws IOException, JSONException {
                //Bukkit.getLogger().info("Token: " + token);
                //Bukkit.getLogger().info("ID: " + id);

                URL url = new URL("https://tiltify.com/api/v3/campaigns/" + id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + token);

                conn.setRequestProperty("Content-Type","application/json");
                conn.setRequestMethod("GET");


                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String output;

                StringBuffer response = new StringBuffer();
                while ((output = in.readLine()) != null) {
                    response.append(output);
                }

                in.close();
                String jsonText = (response.toString());
                JSONObject json = new JSONObject(jsonText);
                double amtRaised = json.getJSONObject("data").getDouble("totalAmountRaised");
                double goal = json.getJSONObject("data").getDouble("fundraiserGoalAmount");
                return new Double[]{amtRaised, goal};
            }

            @Override
            public void run() {
                bar.setProgress(progress);
                bar.setColor(BarColor.valueOf(mainBarColor));
                bar.setTitle(format(mainTitleColor + "Raised $" + donatedAmt.stripTrailingZeros().toPlainString() + " of $" + donationGoal.stripTrailingZeros().toPlainString()));
                progress = amt/goal;
                //Bukkit.getLogger().info("Progress: " + progress);
                if (progress >= 1) {
                    count ++;
                    progress = 1.0;
                    bar.setColor(BarColor.valueOf(goalBarColor));
                    bar.setTitle(format( goalTitleColor + goalMsg));
                    Bukkit.getScheduler().cancelTask(taskID);
                }
            }
        }, 0, 600); //change to 10 or 30s
    }

    private String format(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
