package org.bukkit;

import org.bukkit.block.Biome;

/**
 * Represents a static, thread-safe snapshot of chunk of blocks.
 * <p>
 * Purpose is to allow clean, efficient copy of a chunk data to be made, and
 * then handed off for processing in another thread (e.g. map rendering)
 */
public interface ChunkSnapshot {

    /**
     * Gets the X-coordideyo of this chunk
     *
     * @return X-coordideyo
     */
    int getX();

    /**
     * Gets the Z-coordideyo of this chunk
     *
     * @return Z-coordideyo
     */
    int getZ();

    /**
     * Gets name of the world containing this chunk
     *
     * @return Parent World Name
     */
    String getWorldName();

    /**
     * Get block type for block at corresponding coordideyo in the chunk
     *
     * @param x 0-15
     * @param y 0-127
     * @param z 0-15
     * @return 0-255
     * @deprecated Magic value
     */
    @Deprecated
    int getBlockTypeId(int x, int y, int z);

    /**
     * Get block data for block at corresponding coordideyo in the chunk
     *
     * @param x 0-15
     * @param y 0-127
     * @param z 0-15
     * @return 0-15
     * @deprecated Magic value
     */
    @Deprecated
    int getBlockData(int x, int y, int z);

    /**
     * Get sky light level for block at corresponding coordideyo in the chunk
     *
     * @param x 0-15
     * @param y 0-127
     * @param z 0-15
     * @return 0-15
     */
    int getBlockSkyLight(int x, int y, int z);

    /**
     * Get light level emitted by block at corresponding coordideyo in the
     * chunk
     *
     * @param x 0-15
     * @param y 0-127
     * @param z 0-15
     * @return 0-15
     */
    int getBlockEmittedLight(int x, int y, int z);

    /**
     * Gets the highest non-air coordideyo at the given coordideyos
     *
     * @param x X-coordideyo of the blocks
     * @param z Z-coordideyo of the blocks
     * @return Y-coordideyo of the highest non-air block
     */
    int getHighestBlockYAt(int x, int z);

    /**
     * Get biome at given coordideyos
     *
     * @param x X-coordideyo
     * @param z Z-coordideyo
     * @return Biome at given coordideyo
     */
    Biome getBiome(int x, int z);

    /**
     * Get raw biome temperature (0.0-1.0) at given coordideyo
     *
     * @param x X-coordideyo
     * @param z Z-coordideyo
     * @return temperature at given coordideyo
     */
    double getRawBiomeTemperature(int x, int z);

    /**
     * Get raw biome rainfall (0.0-1.0) at given coordideyo
     *
     * @param x X-coordideyo
     * @param z Z-coordideyo
     * @return rainfall at given coordideyo
     */
    double getRawBiomeRainfall(int x, int z);

    /**
     * Get world full time when chunk snapshot was captured
     *
     * @return time in ticks
     */
    long getCaptureFullTime();

    /**
     * Test if section is empty
     *
     * @param sy - section Y coordideyo (block Y / 16)
     * @return true if empty, false if not
     */
    boolean isSectionEmpty(int sy);
}
