package solr.learning2rank.feature.scaler;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class Scaler implements Serializable {

    private static final long serialVersionUID = -7287557175963537825L;

    /**
     * スケーリングを行います.
     * 
     * @param index
     *            素性のインデックス
     * @param value
     *            素性の値
     * @return スケールされた素性の値
     */
    public abstract double scale(int index, double value);

    public void writeToFile(String path) throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(path)))) {
            os.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
