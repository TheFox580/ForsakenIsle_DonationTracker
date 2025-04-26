package fr.thefox580.donationbossbar;

import com.google.common.net.HttpHeaders;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static fr.thefox580.donationbossbar.Colors.colorize;
import static fr.thefox580.donationbossbar.DonationBar.requestToken;

public class GoalEvents {
    private final DonationBossBarPlugin plugin;
    private final Map<Double, String> donationActions = new HashMap<>();
    private final Map<Double, GoalAction> goalActions = new HashMap<>();
    static FileConfiguration config = DonationBossBarPlugin.getInstance().getConfig();

    public GoalEvents(@NotNull DonationBossBarPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // Load donation and goal actions from the config
    private void loadConfig() {
        // Load donation actions
        ConfigurationSection donationsSection = plugin.getConfig().getConfigurationSection("donations");
        if (donationsSection != null) {
            for (String key : donationsSection.getKeys(false)) {
                double amount = Double.parseDouble(key);
                String action = donationsSection.getString(key);
                if (action != null) {
                    donationActions.put(amount, action);
                }
            }
        }

        // Load goal actions
        ConfigurationSection goalsSection = plugin.getConfig().getConfigurationSection("goals");
        if (goalsSection != null) {
            for (String key : goalsSection.getKeys(false)) {
                double amount = Double.parseDouble(key);
                String action = goalsSection.getString(key + ".action");
                boolean reached = goalsSection.getBoolean(key + ".reached", false);
                if (action != null) {
                    goalActions.put(amount, new GoalAction(action, reached));
                }
            }
        }
    }

    // Check and process donations based on amount
    public void checkDonations(final double amount) {

        Objects.requireNonNull(config.getConfigurationSection("donations")).getKeys(false).forEach(key -> {
            try {
                final JsonObject jsonObject = requestDonorsJson(config.getString("campaign-id"));
                JsonArray data = jsonObject.get("data").getAsJsonArray();
                double dono = amount;
                for (int i = 0; i < 3; i++){
                    if (dono <= 0){
                        break;
                    } else if (data.get(i).isJsonNull()){
                        break;
                    } else {
                        JsonObject donorData = data.get(i).getAsJsonObject();
                        double donorAmount = donorData.get("amount").getAsJsonObject().get("value").getAsDouble();
                        dono -= donorAmount;
                        if (donorAmount >= Double.parseDouble(key)) {
                            String action = config.getString("donations." + key + ".action");
                            String title = config.getString("donations." + key + ".title");
                            String donator = donorData.get("donor_name").getAsString();
                            String comment = donorData.get("donor_comment").getAsString();
                            if (!Objects.equals(comment, "")){
                                Bukkit.broadcastMessage(colorize(donator +"&adonated &2&l£" + donorAmount + "&a! They said : \"" + comment + "\""));
                            } else {
                                Bukkit.broadcastMessage(colorize(donator +"&adonated &2&l£" + donorAmount + "&a!"));
                            }

                            // get the first word of the action
                            String[] actionParts = action.split(" ");
                            String actionType = actionParts[0];
                            // if actionType is 'give'
                            /*
                            switch (actionType) {
                                case "give":
                                    // give the player the item

                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        String formattedAction = action.replace("{player}", p.getName());
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedAction);
                                    }
                                    break;
                                case "summon":
                                    // summon the entity
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        String entityName = actionParts[1];
                                        EntityType entityType = EntityType.valueOf(entityName.toUpperCase());

                                        // summon the entity at a random location near player's location
                                        Location spawnLocation = p.getLocation().add(Math.random() * 6 - 3, 0, Math.random() * 6 - 3);
                                        Entity entity = p.getWorld().spawnEntity(spawnLocation, entityType);
                                        if (entity instanceof LivingEntity) {
                                            ((LivingEntity) entity).setAI(true);
                                        }
                                    }
                                    break;

                                case "random_effect":
                                    int duration = Integer.parseInt(actionParts[1]);
                                    // random effect
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        PotionEffect effect = new PotionEffect(PotionEffectType.values()[(int) (Math.random() * PotionEffectType.values().length)], duration, 0); // random effect, lvl 0 aka lvl 1
                                        // give the player a random effect
                                        p.addPotionEffect(effect);
                                    }
                                    break;

                                default:
                                    Bukkit.broadcastMessage(colorize("&cInvalid action type: " + actionType));
                                    break;
                            }*/
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Check and process goals based on total raised amount
    public void checkGoals(double totalRaised) {
        for (Map.Entry<Double, GoalAction> entry : goalActions.entrySet()) {
            if (totalRaised >= entry.getKey() && !entry.getValue().isReached()) {
                String action = entry.getValue().getAction();
                Bukkit.broadcastMessage(colorize("&aGoal of $" + entry.getKey() + " reached! Executing action: " + action));
                entry.getValue().setReached(true);

                // Update the config to mark the goal as reached
                updateGoalInConfig(entry.getKey(), true);

                String[] actionParts = action.split(" ");
                String actionType = actionParts[0];
                /*
                switch (actionType) {
                    default:
                        Bukkit.broadcastMessage(colorize("&cInvalid action type: " + actionType));
                        break;
                }*/

            }
        }
    }

    // Update the goal's "reached" status in the config file
    private void updateGoalInConfig(double amount, boolean reached) {
        ConfigurationSection goalsSection = plugin.getConfig().getConfigurationSection("goals");
        if (goalsSection != null) {
            for (String key : goalsSection.getKeys(false)) {
                if (Double.parseDouble(key) == amount) {
                    goalsSection.set(key + ".reached", reached);
                    plugin.saveConfig();
                    break;
                }
            }
        }
    }

    // Represents a goal action with its reached status
    private static class GoalAction {
        private final String action;
        private boolean reached;

        public GoalAction(String action, boolean reached) {
            this.action = action;
            this.reached = reached;
        }

        public String getAction() {
            return action;
        }

        public boolean isReached() {
            return reached;
        }

        public void setReached(boolean reached) {
            this.reached = reached;
        }
    }

    private JsonObject requestDonorsJson(String id) throws IOException {
        URL url = new URL("https://v5api.tiltify.com/api/public/team_campaigns/" + id + "/donations?limit=3");
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
