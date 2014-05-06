package solr.learning2rank.feature;

import static java.lang.String.*;

import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

public abstract class FeatureExtractorFactory {

    private static final String INIT_PARAMS = "init";
    private static final String FACTORY_NAME = "factory";

    public static FeatureExtractorFactory getFactory(NamedList<?> params) {

        String factoryName = (String) params.get(FACTORY_NAME);
        if (factoryName == null) {
            throw new SolrException(ErrorCode.BAD_REQUEST,
                    "Unspecified feature extractor factory class name");
        }
        factoryName = factoryName.trim();
        FeatureExtractorFactory factory = null;
        try {
            Class<? extends FeatureExtractorFactory> clzz = Class.forName(
                    factoryName).asSubclass(FeatureExtractorFactory.class);
            factory = clzz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new SolrException(ErrorCode.BAD_REQUEST, format(
                    "Error loading class '%s'", factoryName), e);
        } catch (ClassCastException e) {
            throw new SolrException(ErrorCode.BAD_REQUEST, format(
                    "Extractor factory class must extends %s: %s",
                    FeatureExtractorFactory.class.getName(), factoryName), e);
        } catch (Exception e) {
            throw new SolrException(ErrorCode.BAD_REQUEST, format(
                    "Error creating new instance '%s'", factoryName), e);
        }

        NamedList<?> initParams = (NamedList<?>) params.get(INIT_PARAMS);
        factory.init(initParams);
        return factory;
    }

    public abstract void init(NamedList<?> initParams);

    public abstract FeatureExtractor create(SolrParams params, Query query);
}
