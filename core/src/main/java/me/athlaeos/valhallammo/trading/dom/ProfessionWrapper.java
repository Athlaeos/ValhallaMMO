package me.athlaeos.valhallammo.trading.dom;

import org.bukkit.entity.Villager;

public enum ProfessionWrapper {
    NONE(Villager.Profession.NONE),
    ARMORER(Villager.Profession.ARMORER),
    BUTCHER(Villager.Profession.BUTCHER),
    CARTOGRAPHER(Villager.Profession.CARTOGRAPHER),
    CLERIC(Villager.Profession.CLERIC),
    FARMER(Villager.Profession.FARMER),
    FISHERMAN(Villager.Profession.FISHERMAN),
    FLETCHER(Villager.Profession.FLETCHER),
    LEATHERWORKER(Villager.Profession.LEATHERWORKER),
    LIBRARIAN(Villager.Profession.LIBRARIAN),
    MASON(Villager.Profession.MASON),
    NITWIT(Villager.Profession.NITWIT),
    SHEPHERD(Villager.Profession.SHEPHERD),
    TOOLSMITH(Villager.Profession.TOOLSMITH),
    WEAPONSMITH(Villager.Profession.WEAPONSMITH);

    private final Villager.Profession profession;

    ProfessionWrapper(Villager.Profession profession){
        this.profession = profession;
    }

    public Villager.Profession getProfession() {
        return profession;
    }

    public static ProfessionWrapper ofProfession(Villager.Profession profession){
        return switch (profession){
            case NONE -> NONE;
            case ARMORER -> ARMORER;
            case BUTCHER -> BUTCHER;
            case CARTOGRAPHER -> CARTOGRAPHER;
            case CLERIC -> CLERIC;
            case FARMER -> FARMER;
            case FISHERMAN -> FISHERMAN;
            case FLETCHER -> FLETCHER;
            case LEATHERWORKER -> LEATHERWORKER;
            case LIBRARIAN -> LIBRARIAN;
            case MASON -> MASON;
            case NITWIT -> NITWIT;
            case SHEPHERD -> SHEPHERD;
            case TOOLSMITH -> TOOLSMITH;
            case WEAPONSMITH -> WEAPONSMITH;
        };
    }
}
