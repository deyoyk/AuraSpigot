package me.deyo.spigot.util;

import org.bukkit.Bukkit;

public class CompatHacks {
    private CompatHacks() {
    }

    public static boolean hasProtocolSupport() {
        return Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport");
    }
}