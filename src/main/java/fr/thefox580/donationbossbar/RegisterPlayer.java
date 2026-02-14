package fr.thefox580.donationbossbar;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RegisterPlayer implements Listener {

    private final DonationBossBarPlugin plugin;
    private final DonationBar donationBar;

    public RegisterPlayer(DonationBossBarPlugin plugin, DonationBar donationBar){
        this.plugin = plugin;
        this.donationBar = donationBar;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        Player player = event.getPlayer();

        FileConfiguration config = plugin.getConfig();

        if (!config.getStringList("players_uuid").contains(player.getUniqueId().toString())){
            config.getStringList("players_uuid").add(player.getUniqueId().toString());
        }

        if (donationBar.getBossBar() != null){
            if (!donationBar.getBossBar().getPlayers().contains(player)) {
                donationBar.addPlayer(player);
            }
        }

    }

}
