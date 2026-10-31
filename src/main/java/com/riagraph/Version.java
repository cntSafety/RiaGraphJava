package com.riagraph;

/**
 * Version is a class to get the version of the RiaG.
 */
public class Version {

    /**
     * Get the version of the RiaG.
     *
     * @return The version of the RiaG.
     */
    public static String getVersion() {
        return Native.riagGetVersion();
    }

    /**
     * Get the storage version of the RiaG.
     *
     * @return The storage version of the RiaG.
     */
    public static long getStorageVersion() {
        return Native.riagGetStorageVersion();
    }
}
