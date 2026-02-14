package fr.thefox580.donationbossbar;

import com.google.common.net.HttpHeaders;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.net.*;

public class TiltifyData implements Runnable {

    private final DonationBossBarPlugin plugin = DonationBossBarPlugin.getInstance();
    private final FileConfiguration config;

    private String authToken = "";

    public TiltifyData(){
        this.config = plugin.getConfig();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0L, 7140 * 20L); // Runs every 1h59 hours to replace the token
    }

    @Override
    public void run() {

        URL obj;
        try {

            String client_id = config.getString("client-id");
            String client_secret = config.getString("client-secret");

            obj = new URI("https://v5api.tiltify.com/oauth/token"+
                    "?grant_type=client_credentials"+
                    "&client_id=" + client_id +
                    "&client_secret=" + client_secret +
                    "&scope=public").toURL();
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            JsonElement data = null;
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    data = JsonParser.parseReader(reader);
                }
                if (data != null && !data.isJsonObject()){
                    data = data.getAsJsonObject();
                } else {
                    plugin.getLogger().warning("Could not get the response, the data is null.");
                }
            }
            if (data != null){
                authToken = data.getAsJsonObject().get("access_token").getAsString();
            }

        } catch (IOException | URISyntaxException e) {
            plugin.getLogger().warning(e.toString());
        }

    }

    public JsonObject requestJson(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
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
