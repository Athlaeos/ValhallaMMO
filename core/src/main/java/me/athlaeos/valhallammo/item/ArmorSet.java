package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorSet{
    private final String id;
    private String name = null;
    private List<String> lore = new ArrayList<>();
    private int piecesRequired = 4;
    private Map<String, Double> setBonus = new HashMap<>();

    public ArmorSet(String id){
        this.id = id;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getLore() { return lore; }
    public int getPiecesRequired() { return piecesRequired; }
    public Map<String, Double> getSetBonus() { return setBonus; }

    public void setName(String name) { this.name = name; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public void setPiecesRequired(int piecesRequired) { this.piecesRequired = piecesRequired; }
    public void setSetBonus(Map<String, Double> setBonus) { this.setBonus = setBonus; }
}