package me.athlaeos.valhallammo.dom;

import me.athlaeos.valhallammo.ValhallaMMO;

public enum MinecraftVersion {
    MINECRAFT_1_26(20, "1_26", "1.26"),
    MINECRAFT_1_25(19, "1_25", "1.25"),
    MINECRAFT_1_24(18, "1_24", "1.24"),
    MINECRAFT_1_23(17, "1_23", "1.23"),
    MINECRAFT_1_22(16, "1_22", "1.22"),
    MINECRAFT_1_21(15, "1_21", "1.21"),
    MINECRAFT_1_20_5(14, "1_20_5", "1.20.5"),
    MINECRAFT_1_20(13, "1_20", "1.20"),
    MINECRAFT_1_19(12, "1_19", "1.19"),
    MINECRAFT_1_18(11, "1_18", "1.18"),
    MINECRAFT_1_17(10, "1_17", "1.17"),
    MINECRAFT_1_16(9, "1_16", "1.16"),
    MINECRAFT_1_15(8, "1_15", "1.15"),
    MINECRAFT_1_14(7, "1_14", "1.14"),
    MINECRAFT_1_13(6, "1_13", "1.13"),
    MINECRAFT_1_12(5, "1_12", "1.12"),
    MINECRAFT_1_11(4, "1_11", "1.11"),
    MINECRAFT_1_10(3, "1_10", "1.10"),
    MINECRAFT_1_9(2, "1_9", "1.9"),
    MINECRAFT_1_8(1, "1_8", "1.8"),
    INCOMPATIBLE(-1, null, null);
    private final int v;
    private final String versionString1;
    private final String versionString2;
    private static final MinecraftVersion serverVersion = init();

    private static MinecraftVersion init(){
        String v = ValhallaMMO.getInstance().getServer().getVersion();
        for (MinecraftVersion version : MinecraftVersion.values()){
            if (version.versionString1 == null || version.versionString2 == null) continue;
            if (v.contains(version.versionString1) || v.contains(version.versionString2)) return version;
        }
        return INCOMPATIBLE;
    }

    MinecraftVersion(int v, String v1, String v2){
        this.v = v;
        this.versionString1 = v1;
        this.versionString2 = v2;
    }

    /**
     * @return True if the version is the same or older than the version given
     */
    public static boolean currentVersionOlderThan(MinecraftVersion version){
        if (serverVersion == MinecraftVersion.INCOMPATIBLE) return false;
        return serverVersion.v <= version.v;
    }

    /**
     * @return True if the version is the same or newer than the version given
     */
    public static boolean currentVersionNewerThan(MinecraftVersion version){
        if (serverVersion == MinecraftVersion.INCOMPATIBLE) return false;
        return serverVersion.v >= version.v;
    }

    public static MinecraftVersion getServerVersion() {
        return serverVersion;
    }
}
