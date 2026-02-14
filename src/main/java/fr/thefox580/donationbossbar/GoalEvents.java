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
        final JsonObject jsonObject = plugin.getTiltifyData().requestJson(new URI("https://v5api.tiltify.com/api/public/team_campaigns/" + config.getString("campaign-id") + "/donations?limit=10").toURL());
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
                boolean target = !donorData.get("target_id").isJsonNull();
                String comment = "";
                if (!donorData.get("donor_comment").isJsonNull()){
                    comment = donorData.get("donor_comment").getAsString();
                }
                if (target){
                    String targetId = donorData.get("target_id").getAsString();
                    final JsonObject jsonObjectTarget = plugin.getTiltifyData().requestJson(new URI("https://v5api.tiltify.com/api/public/team_campaigns/" + config.getString("campaign-id") + "/targets?limit=" + 50).toURL());
                    JsonArray targetData = jsonObjectTarget.get("data").getAsJsonArray();
                    JsonObject rightTarget = new JsonObject();
                    for (int j = 0; j < targetData.size(); j++){
                        if (Objects.equals(targetData.get(j).getAsJsonObject().get("id").getAsString(), targetId)){
                            rightTarget = targetData.get(j).getAsJsonObject();
                            break;
                        }
                    }
                    checkReviveTarget(donorAmount, donator, targetId, rightTarget);
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
                boolean target = !donorData.get("target_id").isJsonNull();
                String comment = "";
                if (!donorData.get("donor_comment").isJsonNull()){
                    comment = donorData.get("donor_comment").getAsString();
                }
                if (target){
                    String targetId = donorData.get("target_id").getAsString();
                    final JsonObject jsonObjectTarget = plugin.getTiltifyData().requestJson(new URI("https://v5api.tiltify.com/api/public/team_campaigns/" + config.getString("campaign-id") + "/targets?limit=" + 50).toURL());
                    JsonArray targetData = jsonObjectTarget.get("data").getAsJsonArray();
                    JsonObject rightTarget = new JsonObject();
                    for (int j = 0; j < targetData.size(); j++){
                        if (Objects.equals(targetData.get(j).getAsJsonObject().get("id").getAsString(), targetId)){
                            rightTarget = targetData.get(j).getAsJsonObject();
                        }
                    }
                    checkReviveTarget(donorAmount, donator, targetId, rightTarget);
                }
                announceDonation(donorAmount, donator, comment);
            }
        }  catch (IOException | URISyntaxException e) {
            plugin.getLogger().warning(e.toString());
        }
    }

    private void checkReviveTarget(double donorAmount, String donator, String targetId, JsonObject rightTarget) {
        if (!Objects.equals(rightTarget, new JsonObject())){
            if (rightTarget.get("name").getAsString().startsWith("Revive a Player")){
                String title = rightTarget.get("name").getAsString();
                String username = title.substring(title.lastIndexOf(" ")+1);
                if (rightTarget.get("amount").getAsJsonObject().get("value").getAsDouble() <= rightTarget.get("amount_raised").getAsJsonObject().get("value").getAsDouble()){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"revive " + username);
                    Bukkit.broadcast(colorizeComponent(donator + " &adonated £"+ donorAmount +" and revived &2&l" + username));
                } else {
                    Bukkit.broadcast(colorizeComponent(donator + " &adonated £"+ donorAmount +" to help revive &2&l" + username));
                }
            }
        } else {
            plugin.getLogger().warning("The target id " + targetId + "could not be found.");
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