package me.deyo.spigot.knockback.impl;

import me.deyo.spigot.knockback.KnockbackModifier;
import me.deyo.spigot.knockback.KnockbackProfile;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MathHelper;
import net.minecraft.server.PacketPlayOutEntityVelocity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class SmoothKnockbackProfile extends KnockbackProfile {

    public SmoothKnockbackProfile(String name) {
        super(name);
    }

    @Override
    public String getImplementationName() {
        return "smooth";
    }

    @Override
    public List<KnockbackModifier<?>> getDefaultModifiers() {
        return Arrays.asList(
                new KnockbackModifier<>(double.class, "horizontal", 0.54),
                new KnockbackModifier<>(double.class, "vertical", 0.44),

                new KnockbackModifier<>(double.class, "extra-horizontal", 0.38),
                new KnockbackModifier<>(double.class, "extra-vertical", 0.0),
                new KnockbackModifier<>(double.class, "vertical-limit", 0.365),

                new KnockbackModifier<>(double.class, "vertical-friction", 2.0),
                new KnockbackModifier<>(double.class, "horizontal-friction", 50.0),

                new KnockbackModifier<>(double.class, "start-range", 3.0),
                new KnockbackModifier<>(double.class, "max-range", 0.2),
                new KnockbackModifier<>(double.class, "range-factor", 0.124),

                new KnockbackModifier<>(Integer.class, "no-damage-ticks", 20),
                new KnockbackModifier<>(boolean.class, "sprint-reset", false),

                new KnockbackModifier<>(double.class, "arrow-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "rod-speed", 1.5),
                new KnockbackModifier<>(double.class, "arrow-vertical", 0.4),
                new KnockbackModifier<>(double.class, "egg-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "egg-vertical", 0.4),
                new KnockbackModifier<>(double.class, "pearl-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "pearl-vertical", 0.4),
                new KnockbackModifier<>(double.class, "snowball-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "snowball-vertical", 0.4),
                new KnockbackModifier<>(double.class, "rod-horizontal", 0.4),
                new KnockbackModifier<>(double.class, "rod-vertical", 0.4)
        );
    }

    public double horizontalDistance(Entity victim, Entity source) {
        if (!victim.getBukkitEntity().getWorld().equals(source.getBukkitEntity().getWorld())) {
            return -1.0;
        }
        double d0 = victim.locX - source.locX;
        double d2 = victim.locZ - source.locZ;

        return Math.hypot(d0, d2);
    }

    private double rangeReduction(double range) {
        double startRange = (double) getKnockbackModifier("start-range", false).getValue();
        double maxRange = (double) getKnockbackModifier("max-range", false).getValue();
        double rangeFactor = (double) getKnockbackModifier("range-factor", false).getValue();

        if (range <= startRange) {
            return 0.0;
        }

        double reduction = (range - startRange) * rangeFactor;
        return Math.max(0.0, Math.min(maxRange, reduction));
    }

    @Override
    public void handleEntityLiving(EntityPlayer victim, Entity source, float f, double d0, double d1) {
        if (victim == null || source == null) return;
        
        victim.ai = true;

        double magnitude = MathHelper.sqrt(d0 * d0 + d1 * d1);
        if (magnitude == 0) return;

        // Get base values
        double horizontal = (double) getKnockbackModifier("horizontal", false).getValue();
        double vertical = (double) getKnockbackModifier("vertical", false).getValue();
        double verticalLimit = (double) getKnockbackModifier("vertical-limit", false).getValue();

        // Calculate friction values
        double verticalFriction = (double) getKnockbackModifier("vertical-friction", false).getValue();
        double horizontalFriction = (double) getKnockbackModifier("horizontal-friction", false).getValue();

        // Calculate range reduction
        double range = horizontalDistance(victim, source);
        double rangeReduction = rangeReduction(range);

        // Apply friction
        victim.motX /= horizontalFriction;
        victim.motY /= verticalFriction;
        victim.motZ /= horizontalFriction;

        // Apply horizontal knockback with range reduction
        double effectiveHorizontal = horizontal * (1.0 - rangeReduction);
        victim.motX -= d0 / magnitude * effectiveHorizontal;
        victim.motZ -= d1 / magnitude * effectiveHorizontal;

        // Apply vertical knockback with range reduction
        victim.motY = vertical * (1.0 - rangeReduction);

        // Apply vertical limit
        if (victim.motY > verticalLimit) {
            victim.motY = verticalLimit;
        }
    }

    @Override
    public void handleEntityHuman(EntityPlayer victim, Entity source, int i, Vector vector) {
        double extraHorizontal = (double) getKnockbackModifier("extra-horizontal", false).getValue();

        double range = this.horizontalDistance(victim, source);
        double rangeReduction = this.rangeReduction(range);

        if (i > 0) {
            source.g(
                    (-MathHelper.sin((float) (victim.yaw * Math.PI / 180.0F)) *
                            (float) i
                            * (extraHorizontal - rangeReduction)),
                    (double) getKnockbackModifier("extra-vertical", false).getValue(),
                    (MathHelper.cos((float) (victim.yaw * Math.PI / 180.0F)) *
                            (float) i * (extraHorizontal - rangeReduction))
            );
            // Add sprint lookback and set the LastSprintStatus to false
            if ((boolean) getKnockbackModifier("sprint-reset", false).getValue()) victim.setSprinting(false);
        }

        if (source instanceof EntityPlayer && source.velocityChanged) {
            // CraftBukkit start - Add Velocity Event
            boolean cancelled = false;
            Player player = (Player) source.getBukkitEntity();

            PlayerVelocityEvent event = new PlayerVelocityEvent(player, vector.clone());
            victim.world.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                cancelled = true;
            } else if (!vector.equals(event.getVelocity())) {
                player.setVelocity(event.getVelocity());
            }

            if (!cancelled) {
                ((EntityPlayer) source).playerConnection.sendPacket(new PacketPlayOutEntityVelocity(source));
                source.velocityChanged = false;
                source.motX = vector.getX();
                source.motY = vector.getY();
                source.motZ = vector.getZ();
            }
            // CraftBukkit end
        }
    }

    @Override
    public int getNoDamageTicks() {
        return (int) getKnockbackModifier("no-damage-ticks", false).getValue();
    }

    @Override
    public double getArrowHorizontal() {
        return (double) getKnockbackModifier("arrow-horizontal", false).getValue();
    }

    @Override
    public double getArrowVertical() {
        return (double) getKnockbackModifier("arrow-vertical", false).getValue();
    }

    @Override
    public double getEggHorizontal() {
        return (double) getKnockbackModifier("egg-horizontal", false).getValue();
    }

    @Override
    public double getEggVertical() {
        return (double) getKnockbackModifier("egg-vertical", false).getValue();
    }

    @Override
    public double getRodSpeed() {
        return (double) getKnockbackModifier("rod-speed", false).getValue();
    }

    @Override
    public double getPearlHorizontal() {
        return (double) getKnockbackModifier("pearl-horizontal", false).getValue();
    }

    @Override
    public double getPearlVertical() {
        return (double) getKnockbackModifier("pearl-vertical", false).getValue();
    }

    @Override
    public double getRodHorizontal() {
        return (double) getKnockbackModifier("rod-horizontal", false).getValue();
    }

    @Override
    public double getRodVertical() {
        return (double) getKnockbackModifier("rod-vertical", false).getValue();
    }

    @Override
    public double getSnowballHorizontal() {
        return (double) getKnockbackModifier("snowball-horizontal", false).getValue();
    }

    @Override
    public double getSnowballVertical() {
        return (double) getKnockbackModifier("snowball-vertical", false).getValue();
    }
}

