package fr.thefox580.donationbossbar;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public final class Colors {
    private Colors() {

    }

    public static TextComponent colorizeComponent(String msg) {
        return LegacyComponentSerializer.legacy(LegacyComponentSerializer.AMPERSAND_CHAR).deserialize(msg);
    }

    /**
     * This method stands as Bukkit only takes String for Bossbar titles, but if possible, you should use {@code Color#colorizeComponent}
     */
    @Deprecated(since = "4.0.0")
    public static String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
