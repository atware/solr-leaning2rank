package solr.learning2rank.classifier.liblinear;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.solr.common.util.NamedList;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.feature.RankingFeature;
import solr.learning2rank.feature.scaler.Scaler;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;

public class LiblinearClassifier implements Classifier {

    private final Model model;
    private final Scaler scaler;

    public LiblinearClassifier(Model model, Scaler scaler) {
        this.model = model;
        this.scaler = scaler;
    }

    @Override
    public void init(NamedList<?> params) {
    }

    @Override
    public double getScore(RankingFeature[] target) {
        double[] w = model.getFeatureWeights();
        double score = 0.0;
        for (RankingFeature rankingFeature : target) {
            int idx = rankingFeature.getIndex();
            double value = scaler.scale(idx, rankingFeature.getValue());
            score += w[idx] * value;
        }
        if (model.getBias() > 0) {
            score += w[w.length - 1]; //bias
        }
        return score;
    }
}
