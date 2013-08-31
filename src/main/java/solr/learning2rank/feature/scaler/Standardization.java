package solr.learning2rank.feature.scaler;

/**
 * 以下の式でスケーリングを行うクラスです<br>
 * scaled_value = (value - mean) / standard_deviation
 * 
 * @author atwkwbr
 * 
 */
public class Standardization extends Scaler {

    private static final long serialVersionUID = 5574532697710051749L;

    private double[] meanList;
    private double[] sdList;

    public Standardization() {
    }

    public Standardization(double[] meanList, double[] sdList) {
        this.meanList = meanList;
        this.sdList = sdList;
    }

    @Override
    public double scale(int index, double value) {
        return (value - meanList[index]) / sdList[index];
    }

}
