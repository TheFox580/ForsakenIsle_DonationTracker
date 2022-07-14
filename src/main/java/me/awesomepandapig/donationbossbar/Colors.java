package me.awesomepandapig.donationbossbar;

import org.bukkit.ChatColor;

public final class Colors {
    private Colors() {

    }

    public static String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
