package solr.learning2rank.feature;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;

/**
 * 素性抽出
 * 
 * @author atwkwbr
 * 
 */
public interface FeatureExtractor {

    void setNextReader(AtomicReaderContext context) throws IOException;

    RankingFeature[] extract(int docNum) throws IOException;
}
