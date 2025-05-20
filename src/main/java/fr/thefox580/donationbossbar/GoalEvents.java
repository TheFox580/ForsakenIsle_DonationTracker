package fr.thefox580.donationbossbar;

import com.google.common.net.HttpHeaders;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import static fr.thefox580.donationbossbar.Colors.colorize;
import static fr.thefox580.donationbossbar.DonationBar.requestToken;

public class GoalEvents {
    private final DonationBossBarPlugin plugin;
    static FileConfiguration config = DonationBossBarPlugin.getInstance().getConfig();

    public GoalEvents(@NotNull DonationBossBarPlugin plugin) {
        this.plugin = plugin;
    }

    // Check and process donations based on amount
    public void checkDonations(final double amount) throws IOException {
        final JsonObject jsonObject = requestDonorsJson(config.getString("campaign-id"), 10);
        JsonArray data = jsonObject.get("data").getAsJsonArray();
        double dono = amount;
        for (int i = 0; i < data.size(); i++) {
            if (dono <= 0) {
                break;
            } else if (data.get(i).isJsonNull()) {
                break;
            } else {
                JsonObject donorData = data.get(i).getAsJsonObject();
                double donorAmount = donorData.get("amount").getAsJsonObject().get("value").getAsDouble();
                Bukkit.getLogger().warning("New £"+donorAmount+" donation");
                dono -= donorAmount;
                String donator = donorData.get("donor_name").getAsString();
                boolean target = !donorData.get("target_id").isJsonNull();
                String comment = "";
                if (!donorData.get("donor_comment").isJsonNull()){
                    comment = donorData.get("donor_comment").getAsString();
                }
                if (target){
                    String targetId = donorData.get("target_id").getAsString();
                    final JsonObject jsonObjectTarget = requestTargetsJson(config.getString("campaign-id"), 10);
                    JsonArray targetData = jsonObjectTarget.get("data").getAsJsonArray();
                    JsonObject rightTarget = new JsonObject();
                    for (int j = 0; j < targetData.size(); j++){
                        if (Objects.equals(targetData.get(j).getAsJsonObject().get("id").getAsString(), targetId)){
                            rightTarget = targetData.get(j).getAsJsonObject();
                        }
                    }
                    if (!Objects.equals(rightTarget, new JsonObject())){
                        if (rightTarget.get("title").getAsString().startsWith("Revive a Player")){
                            String title = rightTarget.get("title").getAsString();
                            String username = title.substring(title.lastIndexOf(" ")+1);
                            if (rightTarget.get("amount").getAsJsonObject().get("value").getAsDouble() <= rightTarget.get("amount_raised").getAsJsonObject().get("value").getAsDouble()){
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"revive " + username);
                            }
                            Bukkit.broadcastMessage(colorize(donator + " &adonated £"+ donorAmount +" to try and revive &2&l" + username));
                        }
                    } else {
                        Bukkit.getLogger().warning("The target id " + targetId + "could not be found.");
                    }
                }
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.RECORDS, 1, 1);
                    player.sendTitle("New Donation!", "Look in chat");
                });
                if (!Objects.equals(comment, "")) {
                    Bukkit.broadcastMessage(colorize(donator + " &adonated &2&l£" + donorAmount + "&a and said : \"" + comment + "\""));
                } else {
                    Bukkit.broadcastMessage(colorize(donator + " &adonated &2&l£" + donorAmount + "&a!"));
                }
            }
        }
    }

    public void showDonations(int nbDonos) throws IOException {
        try{
            final JsonObject jsonObject = requestDonorsJson(config.getString("campaign-id"), nbDonos);
            JsonArray data = jsonObject.get("data").getAsJsonArray();
            for (int i = 0; i < nbDonos; i++) {
                JsonObject donorData = data.get(i).getAsJsonObject();
                double donorAmount = donorData.get("amount").getAsJsonObject().get("value").getAsDouble();
                Bukkit.getLogger().warning("New £"+donorAmount+" donation");
                String donator = donorData.get("donor_name").getAsString();
                boolean target = !donorData.get("target_id").isJsonNull();
                String comment = "";
                if (!donorData.get("donor_comment").isJsonNull()){
                    comment = donorData.get("donor_comment").getAsString();
                }
                if (target){
                    String targetId = donorData.get("target_id").getAsString();
                    final JsonObject jsonObjectTarget = requestTargetsJson(config.getString("campaign-id"), 10);
                    JsonArray targetData = jsonObjectTarget.get("data").getAsJsonArray();
                    JsonObject rightTarget = new JsonObject();
                    for (int j = 0; j < targetData.size(); j++){
                        if (Objects.equals(targetData.get(j).getAsJsonObject().get("id").getAsString(), targetId)){
                            rightTarget = targetData.get(j).getAsJsonObject();
                        }
                    }
                    if (!Objects.equals(rightTarget, new JsonObject())){
                        if (rightTarget.get("title").getAsString().startsWith("Revive a Player")){
                            String title = rightTarget.get("title").getAsString();
                            String username = title.substring(title.lastIndexOf(" ")+1);
                            if (rightTarget.get("amount").getAsJsonObject().get("value").getAsDouble() <= rightTarget.get("amount_raised").getAsJsonObject().get("value").getAsDouble()){
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"revive " + username);
                            }
                            Bukkit.broadcastMessage(colorize(donator + " &adonated £"+ donorAmount +" to try and revive &2&l" + username));
                        }
                    } else {
                        Bukkit.getLogger().warning("The target id " + targetId + "could not be found.");
                    }
                }
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.RECORDS, 1, 1);
                    player.sendTitle("New Donation!", "Look in chat");
                });
                if (!Objects.equals(comment, "")) {
                    Bukkit.broadcastMessage(colorize(donator + " &adonated &2&l£" + donorAmount + "&a and said : \"" + comment + "\""));
                } else {
                    Bukkit.broadcastMessage(colorize(donator + " &adonated &2&l£" + donorAmount + "&a!"));
                }
            }
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonObject requestDonorsJson(String id, int nbDonos) throws IOException {
        URL url = new URL("https://v5api.tiltify.com/api/public/team_campaigns/" + id + "/donations?limit=" + nbDonos);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        String token = requestToken().getAsJsonObject().get("access_token").getAsString();
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("GET");

        JsonElement data;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            data = JsonParser.parseReader(reader);
        }
        if (data == null) throw new RuntimeException("Could not get the response, the data is null.");
        if (data.isJsonObject()) return data.getAsJsonObject();
        throw new RuntimeException("Invalid response!");
    }

    private JsonObject requestTargetsJson(String id, int nbTargets) throws IOException {
        URL url = new URL("https://v5api.tiltify.com/api/public/team_campaigns/" + id + "/targets?limit=" + nbTargets);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        String token = requestToken().getAsJsonObject().get("access_token").getAsString();
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("GET");

        JsonElement data;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            data = JsonParser.parseReader(reader);
        }
        if (data == null) throw new RuntimeException("Could not get the response, the data is null.");
        if (data.isJsonObject()) return data.getAsJsonObject();
        throw new RuntimeException("Invalid response!");
    }
}