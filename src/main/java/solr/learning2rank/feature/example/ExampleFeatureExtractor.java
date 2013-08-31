package solr.learning2rank.feature.example;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.common.params.SolrParams;

import solr.learning2rank.feature.FeatureExtractor;
import solr.learning2rank.feature.RankingFeature;

/**
 * Example for FeatureExtractor
 * 
 * @author atwkwbr
 * 
 */
public class ExampleFeatureExtractor implements FeatureExtractor {

    private AtomicReader reader;
    private Query query;

    private static final Set<String> fields = new HashSet<String>();
            
    static {
        fields.add("id");
        fields.add("fan_count");
        fields.add("access_count");
        fields.add("avrg_total");
        fields.add("avrg_food");
        fields.add("avrg_service");
        fields.add("avrg_atomsphere");
        fields.add("avrg_cost");
        fields.add("comment_count");
    }
    
    public ExampleFeatureExtractor(Query query) {
        this.query = query;
    }

    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        reader = context.reader();
    }

    @Override
    public RankingFeature[] extract(int docNum) throws IOException {

        RankingFeature[] features = new RankingFeature[9];

        // tf*idf
        Set<Term> set = new HashSet<Term>();
        query.extractTerms(set);
        Set<String> queryTerms = toStringSet(set);
        Terms terms = reader.getTermVector(docNum, "search_free");
        TermsEnum termsEnum = terms.iterator(null);
        BytesRef text = null;
        double score = 0.0d;
        while ((text = termsEnum.next()) != null) {
            String termStr = text.utf8ToString();
            if (queryTerms.contains(termStr)) {
                Term term = new Term("search_free", termStr);
                double maxDoc = reader.numDocs();
                double docFreq = reader.docFreq(term);
                double freq = termsEnum.totalTermFreq();
                double total = reader.getSumTotalTermFreq("search_free");
                score += (freq / total) * Math.log(maxDoc / docFreq);
            }
        }
        features[0] = new RankingFeature(0, score);
        Document document = reader.document(docNum, fields);
        features[1] = new RankingFeature(1, document.getField("fan_count")
                .numericValue().doubleValue());
        features[2] = new RankingFeature(2, document.getField("access_count")
                .numericValue().doubleValue());
        features[3] = new RankingFeature(3, document.getField("avrg_total")
                .numericValue().doubleValue());
        features[4] = new RankingFeature(4, document.getField("avrg_food")
                .numericValue().doubleValue());
        features[5] = new RankingFeature(5, document.getField("avrg_service")
                .numericValue().doubleValue());
        features[6] = new RankingFeature(6, document
                .getField("avrg_atomsphere").numericValue().doubleValue());
        features[7] = new RankingFeature(7, document.getField("avrg_cost")
                .numericValue().doubleValue());
        features[8] = new RankingFeature(8, document.getField("comment_count")
                .numericValue().doubleValue());

        return features;
    }

    private Set<String> toStringSet(Set<Term> set) {
        Set<String> ret = new HashSet<String>(set.size());
        for (Term term : set) {
            ret.add(term.text());
        }
        return ret;
    }

}
