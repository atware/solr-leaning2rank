package solr.learning2rank;

import java.io.IOException;

import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.search.SortSpec;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.classifier.ClassifierFactory;
import solr.learning2rank.feature.FeatureExtractor;
import solr.learning2rank.feature.FeatureExtractorFactory;

public class PairwiseRankingComponent extends QueryComponent {

    private static final String EXTRACTOR = "extractor";
    public static final String CLASSIFIER = "classifier";

    private FeatureExtractorFactory extractorFactory;
    private ClassifierFactory classifierFactory;

    @SuppressWarnings("rawtypes")
    public void init(NamedList list) {

        NamedList<?> extractorParams = (NamedList<?>) list.get(EXTRACTOR);
        extractorFactory = FeatureExtractorFactory.getFactory(extractorParams);
        NamedList clzzParams = (NamedList) list.get(CLASSIFIER);
        classifierFactory = ClassifierFactory.getFactory(clzzParams);
    }

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {

        super.prepare(rb);
        SolrParams params = rb.req.getParams();
        // ランク学習によってソートするか否かは独自パラメータにて制御する
        if (params.getBool("pairwise_sort", false)) {
            Query query = rb.getQuery();
            FeatureExtractor extractor = extractorFactory.create(params, query);
            Classifier classifier = classifierFactory.createClassifier();
            FieldComparatorSource comparator = new PairwiseRankingComparatorSource(
                    extractor, classifier, params, query);
            SortSpec sortSpec = rb.getSortSpec();
            Sort sort = new Sort(new SortField[] { new SortField("ranking",
                    comparator) });
            // ソート条件が設定されていても上書きする
            sortSpec.setSort(sort);
            rb.getQueryCommand().setSort(sort);
        }
    }
}
