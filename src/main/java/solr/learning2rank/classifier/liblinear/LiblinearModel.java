package solr.learning2rank.classifier.liblinear;

import solr.learning2rank.feature.scaler.Scaler;
import de.bwaldvogel.liblinear.Model;

public class LiblinearModel {

    public LiblinearModel(Model model, Scaler scaler) {
        super();
        this.model = model;
        this.scaler = scaler;
    }

    private final Model model;
    private final Scaler scaler;

    public Model getModel() {
        return model;
    }

    public Scaler getScaler() {
        return scaler;
    }
}
