package solr.learning2rank;

import java.io.*;

import org.apache.lucene.search.*;
import org.apache.solr.common.params.*;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.feature.FeatureExtractor;

public class PairwiseRankingComparatorSource extends FieldComparatorSource {

    private final FeatureExtractor extractor;
    private final Classifier classifier;
    private final SolrParams params;
    private final Query query;

    public PairwiseRankingComparatorSource(FeatureExtractor extractor,
            Classifier classifier, SolrParams params, Query query) {
        this.extractor = extractor;
        this.classifier = classifier;
        this.params = params;
        this.query = query;
    }

    @Override
    public FieldComparator<?> newComparator(String fieldname, int numHits,
            int sortPos, boolean reversed) throws IOException {
        return new PairwiseRankingComparator(params, query, extractor,
                classifier, numHits);
    }
}
