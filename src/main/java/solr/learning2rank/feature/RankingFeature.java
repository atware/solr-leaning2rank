package solr.learning2rank.feature;

/**
 * 素性クラス
 * 
 * @author atwkwbr
 * 
 */
public class RankingFeature {

    private final int index;
    private final double value;

    public RankingFeature(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public double getValue() {
        return value;
    }
}
