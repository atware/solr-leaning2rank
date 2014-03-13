package solr.learning2rank;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.feature.FeatureExtractor;

public class RankingComparator extends FieldComparator<Double> {

    private final FeatureExtractor extractor;
    private final Classifier classifier;

    private double topValue;
    private final double[] topKScores;
    private int bottomSlot;
    private double currentValue;
    private Integer currentDocNum = null;

    public RankingComparator(SolrParams params, Query query,
            FeatureExtractor extractor, Classifier classfiler, int numHit) {
        this.topKScores = new double[numHit];
        this.extractor = extractor;
        this.classifier = classfiler;
    }

    @Override
    public int compare(int slot1, int slot2) {
        return compare(topKScores[slot1], topKScores[slot2]);
    }

    @Override
    public void setBottom(int slot) {
        this.bottomSlot = slot;
    }

    @Override
    public int compareBottom(int doc) throws IOException {
        double bottomValue = topKScores[bottomSlot];
        currentValue = getScore(doc);
        currentDocNum = doc;
        return compare(bottomValue, currentValue);
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
        if (currentDocNum != null && currentDocNum == doc) {
            topKScores[slot] = currentValue;
        } else {
            topKScores[slot] = getScore(doc);
        }
    }

    @Override
    public FieldComparator<Double> setNextReader(AtomicReaderContext context)
            throws IOException {
        extractor.setNextReader(context);
        return this;
    }

    @Override
    public Double value(int slot) {
        return topKScores[slot];
    }

    @Override
    public int compareTop(int doc) throws IOException {
        double value = getScore(doc);
        return compare(topValue, value);
    }

    @Override
    public void setTopValue(Double value) {
        this.topValue = value;
    }

    private int compare(double value1, double value2) {
        if (value1 > value2) {
            return 1;
        } else if (value1 == value2) {
            return 0;
        }
        return -1;
    }

    private double getScore(int doc) throws IOException {
        return classifier.getScore(extractor.extract(doc));
    }
}
