package me.deyo.spigot.command;

import me.deyo.spigot.AuraSpigot;
import me.deyo.spigot.config.AuraConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.deyo.spigot.CC;

import java.util.Arrays;
import java.util.List;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public class AuraVersionCommand extends Command {

    public AuraVersionCommand(String name) {
        super(name);
        setAliases(Arrays.asList("ver", "icanhasbukkit"));
        setPermission("aura.command.version");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        AuraConfig config = AuraConfig.get();
        List<String> versionInfo = config.commands.versionInfo;
        String version = AuraSpigot.get().getVersion();

        for (String message : versionInfo) {
            String formattedMessage = message.replace("{version}", version);
            sender.sendMessage(CC.translate(formattedMessage));
        }

        return true;
    }
}
