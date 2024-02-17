package me.athlaeos.valhallammo.playerstats.format;

import me.athlaeos.valhallammo.playerstats.format.formats.*;

@SuppressWarnings("all")
public abstract class StatFormat {
    public static final StatFormat ROMAN = new RomanNumeralFormat();
    public static final StatFormat PERCENTILE_BASE_1_P1 = new PercentileBase1Format(1);
    public static final StatFormat PERCENTILE_BASE_1_P2 = new PercentileBase1Format(2);
    public static final StatFormat PERCENTILE_BASE_1_P3 = new PercentileBase1Format(3);
    public static final StatFormat PERCENTILE_BASE_1_P4 = new PercentileBase1Format(4);
    public static final StatFormat PERCENTILE_BASE_1_P5 = new PercentileBase1Format(5);
    public static final StatFormat PERCENTILE_BASE_1_P6 = new PercentileBase1Format(6);
    public static final StatFormat PERCENTILE_BASE_100_P1 = new PercentileBase100Format(1);
    public static final StatFormat PERCENTILE_BASE_100_P2 = new PercentileBase100Format(2);
    public static final StatFormat PERCENTILE_BASE_100_P3 = new PercentileBase100Format(3);
    public static final StatFormat PERCENTILE_BASE_100_P4 = new PercentileBase100Format(4);
    public static final StatFormat PERCENTILE_BASE_100_P5 = new PercentileBase100Format(5);
    public static final StatFormat PERCENTILE_BASE_100_P6 = new PercentileBase100Format(6);
    public static final StatFormat SCALAR_BASE_1_P1 = new ScalarBase1Format(1);
    public static final StatFormat SCALAR_BASE_1_P2 = new ScalarBase1Format(2);
    public static final StatFormat INT = new PlainFormat(0);
    public static final StatFormat FLOAT_P1 = new PlainFormat(1);
    public static final StatFormat FLOAT_P2 = new PlainFormat(2);
    public static final StatFormat TIME_SECONDS_BASE_20_P1 = new TimeFormat(1, 20);
    public static final StatFormat TIME_SECONDS_BASE_1000_P1 = new TimeFormat(1, 1000);
    public static final StatFormat DIFFERENCE_INT = new DifferenceFormat(0);
    public static final StatFormat DIFFERENCE_FLOAT_P1 = new DifferenceFormat(1);
    public static final StatFormat DIFFERENCE_FLOAT_P2 = new DifferenceFormat(2);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_1_P1 = new DifferencePercentileBase1Format(1);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_1_P2 = new DifferencePercentileBase1Format(2);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_1_P3 = new DifferencePercentileBase1Format(3);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_1_P4 = new DifferencePercentileBase1Format(4);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_1_P5 = new DifferencePercentileBase1Format(5);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_1_P6 = new DifferencePercentileBase1Format(6);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_100_P1 = new DifferencePercentileBase100Format(1);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_100_P2 = new DifferencePercentileBase100Format(2);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_100_P3 = new DifferencePercentileBase100Format(2);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_100_P4 = new DifferencePercentileBase100Format(3);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_100_P5 = new DifferencePercentileBase100Format(4);
    public static final StatFormat DIFFERENCE_PERCENTILE_BASE_100_P6 = new DifferencePercentileBase100Format(5);
    public static final StatFormat DIFFERENCE_TIME_SECONDS_BASE_20_P1 = new DifferenceTimeFormat(1, 20);
    public static final StatFormat DIFFERENCE_TIME_SECONDS_BASE_1000_P1 = new DifferenceTimeFormat(1, 1000);
    public static final StatFormat NONE = new None();

    public abstract String format(Number stat);
}
