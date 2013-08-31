package solr.learning2rank.feature.scaler;

/**
 * 以下の式でスケーリングを行うクラスです<br>
 * scaled_value = (value - min) / (max - min)
 * 
 * @author atwkwbr
 * 
 */
public class Normalization extends Scaler {

    private static final long serialVersionUID = -1506555094578803300L;

    private double[] minList;
    private double[] maxList;

    public Normalization() {
    }

    public Normalization(double[] minList, double[] maxList) {
        this.minList = minList;
        this.maxList = maxList;
    }

    @Override
    public double scale(int index, double value) {
        return (value - minList[index]) / (maxList[index] - minList[index]);
    }
}
