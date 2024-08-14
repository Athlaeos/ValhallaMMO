package me.athlaeos.valhallammo.dom;

import me.athlaeos.valhallammo.ValhallaMMO;

public enum MinecraftVersion {
    MINECRAFT_1_26(19, "1_26", "1.26", null),
    MINECRAFT_1_25(18, "1_25", "1.25", null),
    MINECRAFT_1_24(17, "1_24", "1.24", null),
    MINECRAFT_1_23(16, "1_23", "1.23", null),
    MINECRAFT_1_22(15, "1_22", "1.22", null),
    MINECRAFT_1_21_1(14.1F, "1_21_1", "1.21.1", "v1_21_R1"),
    MINECRAFT_1_21(14, "1_21", "1.21", "v1_21_R1"),
    MINECRAFT_1_20_6(13.6F, "1_20_6", "1.20.6", "v1_20_R4"),
    MINECRAFT_1_20_5(13.5F, "1_20_5", "1.20.5", "v1_20_R4"),
    MINECRAFT_1_20_4(13.4F, "1_20_4", "1.20.4", "v1_20_R3"),
    MINECRAFT_1_20_3(13.3F, "1_20_3", "1.20.3", "v1_20_R3"),
    MINECRAFT_1_20_2(13.2F, "1_20_2", "1.20.2", "v1_20_R2"),
    MINECRAFT_1_20_1(13.1F, "1_20_1", "1.20.1", "v1_20_R1"),
    MINECRAFT_1_20(13, "1_20", "1.20", "v1_20_R1"),
    MINECRAFT_1_19_4(12.4F, "1_19_4", "1.19.4", "v1_19_R3"),
    MINECRAFT_1_19_3(12.3F, "1_19_3", "1.19.3", "v1_19_R2"),
    MINECRAFT_1_19_2(12.2F, "1_19_2", "1.19.2", "v1_19_R1"),
    MINECRAFT_1_19_1(12.1F, "1_19_1", "1.19.1", "v1_19_R1"),
    MINECRAFT_1_19(12, "1_19", "1.19", "v1_19_R1"),
    MINECRAFT_1_18(11, "1_18", "1.18", null),
    MINECRAFT_1_17(10, "1_17", "1.17", null),
    MINECRAFT_1_16(9, "1_16", "1.16", null),
    MINECRAFT_1_15(8, "1_15", "1.15", null),
    MINECRAFT_1_14(7, "1_14", "1.14", null),
    MINECRAFT_1_13(6, "1_13", "1.13", null),
    MINECRAFT_1_12(5, "1_12", "1.12", null),
    MINECRAFT_1_11(4, "1_11", "1.11", null),
    MINECRAFT_1_10(3, "1_10", "1.10", null),
    MINECRAFT_1_9(2, "1_9", "1.9", null),
    MINECRAFT_1_8(1, "1_8", "1.8", null),
    INCOMPATIBLE(-1, null, null, null);
    private final float v;
    private final String versionString1;
    private final String versionString2;
    private final String nmsVersion;
    private static final MinecraftVersion serverVersion = init();

    private static MinecraftVersion init(){
        String v = ValhallaMMO.getInstance().getServer().getVersion();
        for (MinecraftVersion version : MinecraftVersion.values()){
            if (version.versionString1 == null || version.versionString2 == null) continue;
            if (v.contains(version.versionString1) || v.contains(version.versionString2)) return version;
        }
        return INCOMPATIBLE;
    }

    MinecraftVersion(float v, String v1, String v2, String nmsVersion){
        this.v = v;
        this.versionString1 = v1;
        this.versionString2 = v2;
        this.nmsVersion = nmsVersion;
    }

    public String getVersionString() {
        return versionString1;
    }

    /**
     * @return True if the version is the same or older than the version given
     */
    public static boolean currentVersionOlderThan(MinecraftVersion version){
        if (serverVersion == MinecraftVersion.INCOMPATIBLE) return false;
        return serverVersion.v <= version.v;
    }

    public String getNmsVersion() {
        return nmsVersion;
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
