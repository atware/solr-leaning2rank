package solr.learning2rank.classifier;

import static java.lang.String.*;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.util.NamedList;

public abstract class ClassifierFactory {

    public static ClassifierFactory getFactory(NamedList<?> clzzParams) {

        String factoryName = (String) clzzParams.get("factory");
        if (factoryName == null) {
            throw new SolrException(ErrorCode.BAD_REQUEST,
                    "Unspecified classifier factory class.");
        }
        ClassifierFactory factory = null;
        try {
            Class<? extends ClassifierFactory> clzz = Class
                    .forName(factoryName).asSubclass(ClassifierFactory.class);
            factory = clzz.newInstance();
            NamedList<?> initParams = (NamedList<?>) clzzParams
                    .get("initParams");
            factory.init(initParams);
        } catch (ClassNotFoundException e) {
            throw new SolrException(ErrorCode.BAD_REQUEST, format(
                    "Error loading class '%s'", factoryName), e);
        } catch (ClassCastException e) {
            throw new SolrException(ErrorCode.BAD_REQUEST, format(
                    "Error loading class '%s'", factoryName), e);
        } catch (InstantiationException e) {
            throw new SolrException(ErrorCode.BAD_REQUEST, format(
                    "Error creating new instance '%s'", factoryName), e);
        } catch (IllegalAccessException e) {
            throw new SolrException(ErrorCode.BAD_REQUEST, format(
                    "Error creating new instance '%s'", factoryName), e);
        }
        return factory;
    }

    public abstract void init(NamedList<?> initParams);

    public abstract Classifier createClassifier();
}
