package solr.learning2rank.classifier.libsvm;

import libsvm.svm_model;

import org.apache.solr.common.util.NamedList;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.feature.RankingFeature;
import solr.learning2rank.feature.scaler.Scaler;

public class LibSVMClassifier implements Classifier {

    private final svm_model model;
    private final Scaler scaler;

    public LibSVMClassifier(svm_model model, Scaler scaler) {
        this.model = model;
        this.scaler = scaler;
    }

    @Override
    public void init(NamedList<?> initParams) {
    }

    @Override
    public double getScore(RankingFeature[] target) {
        return 0;
    }

}
