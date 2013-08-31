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
    public int predict(RankingFeature[] left, RankingFeature[] right) {
        Map<Integer, double[]> map = new TreeMap<Integer, double[]>();
        for (RankingFeature feature : left) {
            double[] values = new double[2];
            values[0] = feature.getValue();
            map.put(feature.getIndex(), values);
        }
        for (RankingFeature feature : right) {
            double[] values = map.get(feature.getIndex());
            if (values == null) {
                values = new double[2];
            }
            values[1] = feature.getValue();
        }
        Feature[] fns = new Feature[map.size()];
        int i = 0;
        for (Entry<Integer, double[]> entry : map.entrySet()) {
            int featureIndex = entry.getKey();
            double[] values = entry.getValue();
            double featureValue = values[0] - values[1];
            if (scaler != null) {
                featureValue = scaler.scale(featureIndex, featureValue);
            }
            // Liblinearの素性インデックスは1始まり
            Feature feature = new FeatureNode(featureIndex + 1, featureValue);
            fns[i++] = feature;
        }
        double predict = Linear.predict(model, fns);
        if (predict >= 0.0) {
            return -1;
        } else if (predict == 0.0) {
            return 0;
        }
        return 1;
    }
}
