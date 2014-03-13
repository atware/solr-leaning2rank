package solr.learning2rank.feature.scaler;

/**
 * スケーリングを行わない
 * 
 * @author atware
 * @version 0.0.1
 */
public class NonScaling extends Scaler {

    /** serialVersionUID */
    private static final long serialVersionUID = 1589261498016895451L;

    @Override
    public double scale(int index, double value) {
        return value;
    }
}
