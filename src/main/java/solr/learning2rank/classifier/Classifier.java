package solr.learning2rank.classifier;

import org.apache.solr.common.util.NamedList;

import solr.learning2rank.feature.RankingFeature;

/**
 * ペアワイズ
 * 
 * @author atwkwbr
 * 
 */
public interface Classifier {

    /**
     * インスタンス生成直後に実行される
     * 
     * @param initParams
     */
    void init(NamedList<?> initParams);

    /**
     * @return retun number < 0: leftのdocumentがrightのdocumentより先にソートされる<br>
     *         retun number > 0; leftのdocumentがrightのdocumentより後にソートされる
     */
    int predict(RankingFeature[] left, RankingFeature[] right);
}
