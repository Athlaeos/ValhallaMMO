package me.athlaeos.valhallammo.dom;

import me.athlaeos.valhallammo.ValhallaMMO;

public enum MinecraftVersion {
    MINECRAFT_27_8(16.7F, "27_8", "27.8", "v26_R1"),
    MINECRAFT_27_7(16.6F, "27_7", "27.7", "v26_R1"),
    MINECRAFT_27_6(16.5F, "27_6", "27.6", "v26_R1"),
    MINECRAFT_27_5(16.4F, "27_5", "27.5", "v26_R1"),
    MINECRAFT_27_4(16.3F, "27_4", "27.4", "v26_R1"),
    MINECRAFT_27_3(16.2F, "27_3", "27.3", "v26_R1"),
    MINECRAFT_27_2(16.1F, "27_2", "27.2", "v26_R1"),
    MINECRAFT_27_1(16, "27_1", "27.1", "v26_R1"),
    MINECRAFT_26_8(15.7F, "26_8", "26.8", "v26_R1"),
    MINECRAFT_26_7(15.6F, "26_7", "26.7", "v26_R1"),
    MINECRAFT_26_6(15.5F, "26_6", "26.6", "v26_R1"),
    MINECRAFT_26_5(15.4F, "26_5", "26.5", "v26_R1"),
    MINECRAFT_26_4(15.3F, "26_4", "26.4", "v26_R1"),
    MINECRAFT_26_3(15.2F, "26_3", "26.3", "v26_R1"),
    MINECRAFT_26_2(15.1F, "26_2", "26.2", "v26_R1"),
    MINECRAFT_26_1(15, "26_1", "26.1", "v26_R1"),
    MINECRAFT_1_21_11(14.92F, "1_21_11", "1.21.11", "v1_21_R7", "v1_21_R5"),
    MINECRAFT_1_21_10(14.91F, "1_21_10", "1.21.10", "v1_21_R6", "v1_21_R5"),
    MINECRAFT_1_21_9(14.9F, "1_21_9", "1.21.9", "v1_21_R6", "v1_21_R5"),
    MINECRAFT_1_21_8(14.8F, "1_21_8", "1.21.8", "v1_21_R5"),
    MINECRAFT_1_21_7(14.7F, "1_21_7", "1.21.7", "v1_21_R5"),
    MINECRAFT_1_21_6(14.6F, "1_21_6", "1.21.6", "v1_21_R5"),
    MINECRAFT_1_21_5(14.5F, "1_21_5", "1.21.5", "v1_21_R4"),
    MINECRAFT_1_21_4(14.4F, "1_21_4", "1.21.4", "v1_21_R3"),
    MINECRAFT_1_21_3(14.3F, "1_21_3", "1.21.3", "v1_21_R2"),
    MINECRAFT_1_21_2(14.2F, "1_21_2", "1.21.2", "v1_21_R1"),
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
    INCOMPATIBLE(-1, null, null, null);
    private final float v;
    private final String versionString1;
    private final String versionString2;
    private final String nmsVersion;
    private final String paperVersion;
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
        this.paperVersion = nmsVersion;
    }

    MinecraftVersion(float v, String v1, String v2, String nmsVersion, String paperVersion){
        this.v = v;
        this.versionString1 = v1;
        this.versionString2 = v2;
        this.nmsVersion = nmsVersion;
        this.paperVersion = paperVersion;
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

    public String getPaperVersion() {
        return paperVersion;
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
