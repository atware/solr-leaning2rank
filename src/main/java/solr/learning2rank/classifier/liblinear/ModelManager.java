package solr.learning2rank.classifier.liblinear;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelManager implements ModelManagerMBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicReference<LiblinearModel> model = new AtomicReference<>();

    @Override
    public void setModel(LiblinearModel modelAndScaler) {
        model.set(modelAndScaler);
        logger.info(String.format("New model is installed:\n%s",
                model.toString()));
    }

    @Override
    public LiblinearModel getModel() {
        return model.get();
    }
}
