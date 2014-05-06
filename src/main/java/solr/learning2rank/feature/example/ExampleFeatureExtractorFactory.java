package solr.learning2rank.feature.example;

import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

import solr.learning2rank.feature.FeatureExtractor;
import solr.learning2rank.feature.FeatureExtractorFactory;

public class ExampleFeatureExtractorFactory extends FeatureExtractorFactory {

    @Override
    public void init(NamedList<?> initParams) {
    }

    @Override
    public FeatureExtractor create(SolrParams params, Query query) {
        return new ExampleFeatureExtractor(query);
    }
}
