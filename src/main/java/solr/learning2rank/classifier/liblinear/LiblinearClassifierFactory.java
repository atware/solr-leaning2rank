package solr.learning2rank.classifier.liblinear;

import static java.lang.String.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.util.NamedList;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.classifier.ClassifierFactory;
import solr.learning2rank.feature.scaler.Scaler;
import de.bwaldvogel.liblinear.Model;

public class LiblinearClassifierFactory extends ClassifierFactory {

    private final ModelManagerMBean manager = new ModelManager();

    @Override
    public void init(NamedList<?> initParams) {

        // init model file
        Model model = null;
        String modelFile = (String) initParams.get("model");
        try {
            model = Model.load(new File(modelFile));
        } catch (IOException e) {
            new SolrException(ErrorCode.BAD_REQUEST, format(
                    "Error loading model file '%s'", modelFile), e);
        }

        // init scaler
        Scaler scaler = null;
        String serializedFile = (String) initParams.get("scaler");
        if (serializedFile != null) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(serializedFile)))) {
                scaler = (Scaler) ois.readObject();
            } catch (Exception e) {
                new SolrException(ErrorCode.BAD_REQUEST, format(
                        "Error loading scaler'%s'", serializedFile), e);
            }
        }
        manager.setModel(new LiblinearModel(model, scaler));

        // setting for replacing model and scaler with newer via JMX
        String jmx = (String) initParams.get("jmx");
        if (jmx != null) {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            try {
                ObjectName name = new ObjectName(
                        "solr.learning2rank.impl.liblinear:name=ModelManager");
                server.registerMBean(manager, name);
            } catch (Exception e) {
                throw new SolrException(ErrorCode.SERVER_ERROR, e);
            }
        }
    }

    @Override
    public Classifier createClassifier() {
        LiblinearModel model = manager.getModel();
        return new LiblinearClassifier(model.getModel(), model.getScaler());
    }
}
