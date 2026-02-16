package fr.thefox580.donationbossbar;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static fr.thefox580.donationbossbar.Colors.colorizeComponent;

public class GoalEvents {
    private final DonationBossBarPlugin plugin;
    static FileConfiguration config = DonationBossBarPlugin.getInstance().getConfig();

    public GoalEvents(@NotNull DonationBossBarPlugin plugin) {
        this.plugin = plugin;
    }

    // Check and process donations based on amount
    public void checkDonations(final double amount) throws IOException, URISyntaxException {
        final JsonObject jsonObject = plugin.getTiltifyData().requestJson(new URI(" " + config.getString("campaign-id") + "/donations?limit=10").toURL());
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
                plugin.getLogger().warning("New £"+donorAmount+" donation");
                dono -= donorAmount;
                String donator = donorData.get("donor_name").getAsString();
                boolean reward = !donorData.get("reward_id").isJsonNull();
                String comment = "";
                if (!donorData.get("donor_comment").isJsonNull()){
                    comment = donorData.get("donor_comment").getAsString();
                }
                if (reward){
                    String rewardId = donorData.get("reward_id").getAsString();
                    final JsonObject jsonObjectReward = plugin.getTiltifyData().requestJson(new URI("https://v5api.tiltify.com/api/public/team_campaigns/" + config.getString("campaign-id") + "/rewards?limit=" + 50).toURL());
                    JsonArray rewardData = jsonObjectReward.get("data").getAsJsonArray();
                    JsonObject rightReward = new JsonObject();
                    for (int j = 0; j < rewardData.size(); j++){
                        if (Objects.equals(rewardData.get(j).getAsJsonObject().get("id").getAsString(), rewardId)){
                            rightReward = rewardData.get(j).getAsJsonObject();
                            break;
                        }
                    }
                    checkReviveReward(donorAmount, donator, rewardId, rightReward);
                }
                announceDonation(donorAmount, donator, comment);
            }
        }
    }

    public void checkDonations(int nbDonos) throws IOException {
        try{
            final JsonObject jsonObject = plugin.getTiltifyData().requestJson(new URI("https://v5api.tiltify.com/api/public/team_campaigns/" + config.getString("campaign-id") + "/donations?limit=" + nbDonos).toURL());
            JsonArray data = jsonObject.get("data").getAsJsonArray();
            for (int i = 0; i < nbDonos; i++) {
                JsonObject donorData = data.get(i).getAsJsonObject();
                double donorAmount = donorData.get("amount").getAsJsonObject().get("value").getAsDouble();
                plugin.getLogger().warning("New £"+donorAmount+" donation");
                String donator = donorData.get("donor_name").getAsString();
                boolean reward = !donorData.get("reward_id").isJsonNull();
                String comment = "";

                if (!donorData.get("donor_comment").isJsonNull()){
                    comment = donorData.get("donor_comment").getAsString();
                }

                if (reward){
                    String rewardId = donorData.get("reward_id").getAsString();
                    final JsonObject jsonObjectReward = plugin.getTiltifyData().requestJson(new URI("https://v5api.tiltify.com/api/public/team_campaigns/" + config.getString("campaign-id") + "/rewards?limit=" + 50).toURL());
                    JsonArray rewardData = jsonObjectReward.get("data").getAsJsonArray();
                    JsonObject rightReward = new JsonObject();
                    for (int j = 0; j < rewardData.size(); j++){
                        if (Objects.equals(rewardData.get(j).getAsJsonObject().get("id").getAsString(), rewardId)){
                            rightReward = rewardData.get(j).getAsJsonObject();
                        }
                    }

                    checkReviveReward(donorAmount, donator, rewardId, rightReward);
                }
                announceDonation(donorAmount, donator, comment);
            }
        }  catch (IOException | URISyntaxException e) {
            plugin.getLogger().warning(e.toString());
        }
    }

    private void checkReviveReward(double donorAmount, String donator, String rewardId, JsonObject rightReward) {
        if (!Objects.equals(rightReward, new JsonObject())){
            if (rightReward.get("name").getAsString().startsWith("Revive")){
                String title = rightReward.get("name").getAsString();
                String username = title.substring(title.lastIndexOf(" ")+1);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"revive " + username);
                Bukkit.broadcast(colorizeComponent(donator + " &adonated £"+ donorAmount +" and revived &2&l" + username));
            }
        } else {
            plugin.getLogger().warning("The reward id \"" + rewardId + "\" could not be found.");
        }
    }

    private void announceDonation(double donorAmount, String donator, String comment) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.RECORDS, 1, 1);
            player.showTitle(
                    Title.title(
                            Component.text("New Donation!"),
                            Component.text("Look in chat")
                    )
            );
        });
        if (!Objects.equals(comment, "")) {
            Bukkit.broadcast(colorizeComponent(donator + " &adonated &2&l£" + donorAmount + "&a and said : \"" + comment + "\""));
        } else {
            Bukkit.broadcast(colorizeComponent(donator + " &adonated &2&l£" + donorAmount + "&a!"));
        }
    }
}