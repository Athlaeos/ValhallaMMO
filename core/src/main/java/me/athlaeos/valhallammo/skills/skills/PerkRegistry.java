package me.athlaeos.valhallammo.skills.skills;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PerkRegistry {
    private static Map<String, Perk> allPerks = Collections.unmodifiableMap(new HashMap<>());

    public static Map<String, Perk> getAllPerks() {
        return allPerks;
    }

    public static void registerPerk(Perk p){
        Map<String, Perk> perks = new HashMap<>(allPerks);
        perks.put(p.getName(), p);
        allPerks = Collections.unmodifiableMap(perks);
    }

    public static Perk getPerk(String name){
        return allPerks.get(name);
    }

    public static void clearRegistry(){
        allPerks = Collections.unmodifiableMap(new HashMap<>());
    }
}
