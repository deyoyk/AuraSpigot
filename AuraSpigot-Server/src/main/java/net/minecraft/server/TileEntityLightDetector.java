package net.minecraft.server;

import me.deyo.spigot.config.AuraWorldConfig.Optimizations.Ticking.LightDetector; // AuraSpigot

public class TileEntityLightDetector extends TileEntity implements IUpdatePlayerListBox {

    public TileEntityLightDetector() {}

    public void c() {
        // AuraSpigot start
        if (this.world == null) return;
        LightDetector config = this.world.auraConfig.optimizations.ticking.lightDetector;
        if (!config.tick) return;
        if (!this.world.isClientSide && this.world.getTime() % config.checkInterval == 0L) {
        // AuraSpigot end
            this.e = this.w();
            if (this.e instanceof BlockDaylightDetector) {
                ((BlockDaylightDetector) this.e).f(this.world, this.position);
            }
        }

    }
}
