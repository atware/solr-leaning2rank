package solr.learning2rank;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.feature.FeatureExtractor;
import solr.learning2rank.feature.RankingFeature;

public class PairwiseRankingComparator extends FieldComparator<Double> {

    private final FeatureExtractor extractor;
    private final Classifier classifier;

    // features of top docs
    private final RankingFeature[][] topFeatures;
    // slot of bottom
    private int bottomSlot;

    private RankingFeature[] currentDocFeature;
    private Integer currentDocNum = null;

    public PairwiseRankingComparator(SolrParams params, Query query,
            FeatureExtractor extractor, Classifier classfiler, int numHit) {
        this.extractor = extractor;
        this.classifier = classfiler;
        topFeatures = new RankingFeature[numHit][];
    }

    @Override
    public int compare(int slot1, int slot2) {
        return classifier.predict(topFeatures[slot1], topFeatures[slot2]);
    }

    @Override
    public void setBottom(int slot) {
        this.bottomSlot = slot;
    }

    @Override
    public int compareBottom(int doc) throws IOException {
        currentDocFeature = extractor.extract(doc);
        currentDocNum = doc;
        return classifier.predict(topFeatures[bottomSlot], currentDocFeature);
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
        if (currentDocNum != null && currentDocNum == doc) {
            topFeatures[slot] = currentDocFeature;
        } else {
            topFeatures[slot] = extractor.extract(doc);
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
        return 0.0;
    }

    @Override
    public int compareDocToValue(int doc, Double value) throws IOException {
        return 0;
    }

}
