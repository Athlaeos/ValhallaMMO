package me.athlaeos.valhallammo.trading;

import java.util.Collection;
import java.util.HashSet;

public class MerchantType {
    private final String type;
    private String name = null;
    private boolean realistic = false; // if realistic, villagers reset their trades when they restock
    private boolean perPlayerStock = false; // if perPlayerStock, each player can trade up to the trade's limit times
    private double weight = 10; // the weight determines how likely this merchant type is to be picked as a new villager's type
    private double noviceTradeRolls = 2; // the amount of times custom trades will be rolled when the villager gets their novice trades
    private double noviceTradeRollQuality = 0; // quality is really a trade roll modifier scaling with luck, the formula for total trade rolls is finalRolls = rolls + (quality * luck). generally speaking, better luck = more trades
    private Collection<String> noviceTrades = new HashSet<>(); // possible trades for novices. must always refer to MerchantTrade id's
    private double apprenticeTradeRolls = 2; // the amount of times custom trades will be rolled when the villager gets their apprentice trades
    private double apprenticeTradeRollQuality = 0;
    private Collection<String> apprenticeTrades = new HashSet<>();
    private double journeymanTradeRolls = 2; // and so forth
    private double journeymanTradeRollQuality = 0;
    private Collection<String> journeymanTrades = new HashSet<>();
    private double expertTradeRolls = 2;
    private double expertTradeRollQuality = 0;
    private Collection<String> expertTrades = new HashSet<>();
    private double masterTradeRolls = 2;
    private double masterTradeRollQuality = 0;
    private Collection<String> masterTrades = new HashSet<>();

    public MerchantType(String type){
        this.type = type;
    }
}
