package me.deyo.spigot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import me.deyo.spigot.command.*;
import me.deyo.spigot.command.plugin.AuraPluginsCommand;
import me.deyo.spigot.config.*;
import me.deyo.spigot.knockback.KnockbackAPI;
import me.deyo.spigot.knockback.KnockbackAPIServerDelegate;
import me.deyo.spigot.knockback.KnockbackHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.World;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.defaults.PluginsCommand;
import org.bukkit.command.defaults.VersionCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import me.deyo.spigot.config.serdes.WarningStateTransformer;
import me.deyo.spigot.knockback.KnockbackAPIDelegate;
import me.deyo.spigot.motion.MotionHandler;
import me.deyo.spigot.util.Pair;
import org.spigotmc.RestartCommand;
import org.spigotmc.TicksPerSecondCommand;
import me.deyo.spigot.util.yaml.YamlConfig;

public class AuraSpigot {

    private static AuraSpigot INSTANCE;
    private static String version = "1.6.1";

    private final MinecraftServer server;

    private final KnockbackHandler knockbackHandler;
    private final KnockbackAPIServerDelegate knockbackAPIServerDelegate;
    private final KnockbackAPIDelegate knockbackAPIDelegate;
    private final YamlConfig knockbackConfig;

    private final MotionHandler motionHandler;

    private final YamlConfiguration auraConfiguration = new YamlConfiguration();
    private final YamlConfiguration bukkitConfiguration = new YamlConfiguration();
    private final YamlConfiguration spigotConfiguration = new YamlConfiguration();
    private final YamlConfiguration paperConfiguration = new YamlConfiguration();
    private final YamlConfiguration tacoConfiguration = new YamlConfiguration();
    private AuraConfig auraConfig;
    private BukkitConfig bukkitConfig;
    private Config spigotConfig;
    private PaperConfig paperConfig;
    private TacoConfig tacoConfig;
    private boolean isReload = false;

    private final Map<String, Pair<String, Command>> commands = new HashMap<>();

    public AuraSpigot(@NotNull MinecraftServer server) {
        INSTANCE = this;

        this.server = server;

        knockbackConfig = new YamlConfig("knockback.yml");
        knockbackHandler = new KnockbackHandler();
        knockbackAPIServerDelegate = new KnockbackAPIServerDelegate(knockbackHandler);
        knockbackAPIDelegate = knockbackAPIServerDelegate;
        KnockbackAPI.registerDelegate(knockbackAPIDelegate);

        motionHandler = new MotionHandler();
    }

    public void init() {
        // Configs
        ConfigurationFactory configurationFactory = new ConfigurationFactory(this.server);
        this.auraConfig = configurationFactory.createServerConfig(AuraConfig.class, this.auraConfiguration, "aura-settings", it -> it.withSerdesPack(registry -> registry.register(new WarningStateTransformer())));
        this.bukkitConfig = configurationFactory.createServerConfig(BukkitConfig.class, this.bukkitConfiguration, "bukkit-settings");
        this.spigotConfig = configurationFactory.createServerConfig(Config.class, this.spigotConfiguration, "spigot-settings");
        this.paperConfig = configurationFactory.createServerConfig(PaperConfig.class, this.paperConfiguration, "paper-settings");
        this.tacoConfig = configurationFactory.createServerConfig(TacoConfig.class, this.tacoConfiguration, "taco-settings");
        this.remapConfigs();

        // Commands
        this.registerCommand("restart", "Spigot", new RestartCommand("restart"));
        this.registerCommand("tps", "Spigot", new TicksPerSecondCommand("tps"));
        this.registerCommand("aura", new AuraCommand());
        this.registerCommand("knockback", new KnockbackCommand());
        this.registerCommand("motion", new MotionCommand());
        this.registerCommand("wipe", new EntitiesWipeCommand());
        this.registerCommand("ping", new PingCommand());

        if (AuraConfig.get().commands.auraVersionCommand) {
            this.registerCommand("version", new AuraVersionCommand("version"));
        } else if (AuraConfig.get().commands.versionCommandVanilla) {
            this.registerCommand("bukkit", new VersionCommand("version"));
        }

        if (AuraConfig.get().commands.auraPluginsCommand) {
            this.registerCommand("plugin", new AuraPluginsCommand("plugin"));
        } else if (AuraConfig.get().commands.pluginsCommandVanilla) {
            this.registerCommand("bukkit", new PluginsCommand("plugins"));
        }
    }

    public @NotNull YamlConfiguration getAuraSpigotConfiguration() {
        return this.auraConfiguration;
    }

    public @NotNull AuraConfig getAuraSpigotConfig() {
        return Objects.requireNonNull(this.auraConfig, "AuraSpigotConfig not initialized");
    }

    public @NotNull YamlConfiguration getBukkitConfiguration() {
        return this.bukkitConfiguration;
    }

    public @NotNull BukkitConfig getBukkitConfig() {
        return Objects.requireNonNull(this.bukkitConfig, "BukkitConfig not initialized");
    }

    public @NotNull YamlConfiguration getSpigotConfiguration() {
        return this.spigotConfiguration;
    }

    public @NotNull Config getSpigotConfig() {
        return Objects.requireNonNull(this.spigotConfig, "SpigotConfig not initialized");
    }

    public @NotNull YamlConfiguration getPaperConfiguration() {
        return this.paperConfiguration;
    }

    public @NotNull PaperConfig getPaperConfig() {
        return Objects.requireNonNull(this.paperConfig, "PaperConfig not initialized");
    }

    public @NotNull YamlConfiguration getTacoConfiguration() {
        return this.tacoConfiguration;
    }

    public @NotNull TacoConfig getTacoConfig() {
        return Objects.requireNonNull(this.tacoConfig, "TacoConfig not initialized");
    }

    public void reloadConfigs() {
        this.isReload = true;

        this.auraConfig.load();
        this.bukkitConfig.load();
        this.spigotConfig.load();
        this.paperConfig.load();
        this.tacoConfig.load();

        this.server.worlds.forEach(server -> server.antiXray = this.spigotConfig.getWorldConfig(server.getWorld().getName()).getAntiXray());

        this.remapConfigs();

        this.isReload = false;
    }

    public void remapConfigs() {
        this.auraConfig.remap();
        this.spigotConfig.remap();
        this.paperConfig.remap();
        this.tacoConfig.remap();
        this.server.worlds.forEach(this::remapConfigs);
    }

    public void remapConfigs(@NotNull World world) {
        String worldName = world.getWorldName();
        this.auraConfig.getWorldConfig(worldName).remap(world.auraConfig);
        this.spigotConfig.getWorldConfig(worldName).remap(world.spigotConfig);
        this.paperConfig.getWorldConfig(worldName).remap(world.paperSpigotConfig);
    }

    public void registerCommand(@NotNull String name, @NotNull String fallbackPrefix, @NotNull Command command) {
        this.commands.put(name, Pair.of(fallbackPrefix, command));
    }

    public void registerCommand(@NotNull String name, @NotNull Command command) {
        this.registerCommand(name, "AuraSpigot", command);
    }

    public void registerCommandsInServer() {
        this.commands.forEach((key, command) -> this.server.server
                .getCommandMap()
                .register(key, command.getLeft(), command.getRight()));
    }

    public boolean isProxyOnlineMode() {
        return Bukkit.getOnlineMode() || (this.spigotConfig.settings.bungeecord && this.auraConfig.settings.proxyOnlineMode);
    }

    public static @NotNull AuraSpigot get() {
        return Objects.requireNonNull(INSTANCE, "AuraSpigot not initialized");
    }

    public MotionHandler getMotionHandler() {
        return motionHandler;
    }

    public YamlConfig getKnockbackConfig() {
        return knockbackConfig;
    }

    public KnockbackHandler getKnockbackHandler() {
        return knockbackHandler;
    }

    public static String getVersion() {
        return version;
    }
}
