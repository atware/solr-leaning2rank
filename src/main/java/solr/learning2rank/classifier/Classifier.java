package solr.learning2rank.classifier;

import org.apache.solr.common.util.NamedList;

import solr.learning2rank.feature.RankingFeature;

/**
 * ペアワイズ
 * 
 * @author atwkwbr
 */
public interface Classifier {

    /**
     * インスタンス生成直後に実行される
     * 
     * @param initParams
     */
    void init(NamedList<?> initParams);

    /**
     * w*x
     * 
     * @param target
     * @return
     */
    double getScore(RankingFeature[] target);
}
