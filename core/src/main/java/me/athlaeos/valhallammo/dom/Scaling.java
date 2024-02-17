package me.athlaeos.valhallammo.dom;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.configuration.file.YamlConfiguration;

public class Scaling {
    private Double lowerBound;
    private Double upperBound;
    private final ScalingMode scalingType;
    private final String expression;

    /**
     * Constructs an Expression with lower and upper bound. <br>
     * The lower bound must always be lower than the upper bound, if not, their values are swapped. <br>
     * @param expression the expression for evaluation
     * @param applicationType how the expression should interact with values outside of it, if at all.
     * @param lowerBound the lower bound of the formula. Outcome of expression can never go below this.
     * @param upperBound the upper bound of the formula. Outcome of expression can never go above this.
     */
    public Scaling(String expression, ScalingMode applicationType, Double lowerBound, Double upperBound) {
        this.expression = expression;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        if (lowerBound != null && upperBound != null) {
            this.lowerBound = Math.min(upperBound, lowerBound);
            this.upperBound = Math.max(upperBound, lowerBound);
        }
        this.scalingType = applicationType;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public ScalingMode getScalingType() {
        return scalingType;
    }

    public String getExpression() {
        return expression;
    }

    public double evaluate(String expression){
        double result = Utils.eval(expression);
        if (lowerBound != null) result = Math.max(lowerBound, result);
        if (upperBound != null) result = Math.min(upperBound, result);
        return result;
    }

    public double evaluate(String expression, double applyOn){
        double result = Utils.eval(expression);
        if (lowerBound != null) result = Math.max(lowerBound, result);
        if (upperBound != null) result = Math.min(upperBound, result);
        return scalingType == null ? result : switch (scalingType) {
            case MULTIPLIER -> applyOn * result;
            case ADD_ON_DEFAULT -> applyOn + result;
        };
    }

    public enum ScalingMode {
        MULTIPLIER,
        ADD_ON_DEFAULT
    }

    public static Scaling fromConfig(String config, String path){
        YamlConfiguration c = ConfigManager.getConfig(config).get();
        String value = c.getString(path);
        String def = null;
        c = ConfigManager.getDefault(config);
        if (c != null) def = c.getString(path);

        if (value == null) return null;
        try {
            return fromString(value, def);
        } catch (IllegalArgumentException e){
            ValhallaMMO.logWarning("At " + config + ":" + path + ", " + e.getMessage());
            return null;
        }
    }

    public static Scaling fromString(String value, String def) throws IllegalArgumentException {
        if (value == null) value = def;
        if (value == null) return null;
        String[] args = value.split(",");
        if (args.length != 4) {
            throw new IllegalArgumentException("Scaling needs 4 arguments separated by comma, " + args.length + " were found. Valid format: 'EXPRESSION,MODE,LOWERBOUND,UPPERBOUND'");
        }
        Scaling.ScalingMode mode;
        try { mode = Scaling.ScalingMode.valueOf(args[1]); } catch (IllegalArgumentException ignored){
            throw new IllegalArgumentException("Unrecognized ScalingMode " + args[1] + " used. Valid formats are 'MULTIPLIER', 'ADD_ON_DEFAULT'");
        }

        Double lower;
        try { lower = Double.valueOf(args[2]); } catch (NumberFormatException ignored) { lower = null; }

        Double upper;
        try { upper = Double.valueOf(args[3]); } catch (NumberFormatException ignored) { upper = null; }

        return new Scaling(args[0], mode, lower, upper);
    }
}
