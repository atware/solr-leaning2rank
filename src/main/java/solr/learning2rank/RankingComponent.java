package solr.learning2rank;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortRescorer;
import org.apache.lucene.search.TopDocs;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.response.ResultContext;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.SortSpec;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.classifier.ClassifierFactory;
import solr.learning2rank.feature.FeatureExtractor;
import solr.learning2rank.feature.FeatureExtractorFactory;

public class RankingComponent extends QueryComponent {

    public static final String EXTRACTOR = "extractor";
    public static final String CLASSIFIER = "classifier";

    private FeatureExtractorFactory extractorFactory;
    private ClassifierFactory classifierFactory;

    @SuppressWarnings("rawtypes")
    public void init(NamedList list) {
        NamedList<?> extractorParams = (NamedList<?>) list.get(EXTRACTOR);
        extractorFactory = FeatureExtractorFactory.getFactory(extractorParams);
        NamedList clzzParams = (NamedList) list.get(CLASSIFIER);
        classifierFactory = ClassifierFactory.getFactory(clzzParams);
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {
        SolrParams params = rb.req.getParams();
        boolean rescore = params.getBool("rescore", false);
        if (rescore) {
            int firstSize = params.getInt("first_pass_size", 1000);
            SortSpec sortSpec = rb.getSortSpec();
            int len = sortSpec.getCount();
            int offset = sortSpec.getOffset();
            if (offset + len > firstSize) {
                Sort sort = createSort(rb, params);
                List<SchemaField> fields = Arrays.asList(new SchemaField[1]);
                sortSpec.setSortAndFields(sort, fields);
                rb.getQueryCommand().setSort(sort);
                super.process(rb);
            } else {
                sortSpec.setCount(firstSize);
                sortSpec.setOffset(0);
                super.process(rb);
                Sort sort = createSort(rb, params);
                TopDocs firstPassTopDocs = restoreTopDocs(rb.getResults().docList);
                TopDocs topDocs = new SortRescorer(sort).rescore(
                        rb.req.getSearcher(), firstPassTopDocs, offset + len);
                setRescoredResults(rb, topDocs, offset, len);
            }
        } else {
            super.process(rb);
        }
    }

    /**
     * restore <code>TopDocs</code> from <code>DocListAndSet</code>
     * 
     * @param results
     * @return
     */
    private TopDocs restoreTopDocs(DocList docList) {
        int idx = 0;
        ScoreDoc[] scoreDocs = new ScoreDoc[docList.size()];
        DocIterator iterator = docList.iterator();
        while (iterator.hasNext()) {
            int docId = iterator.nextDoc();
            scoreDocs[idx++] = new ScoreDoc(docId, -1);
        }
        return new TopDocs(docList.matches(), scoreDocs, -1);
    }

    private Sort createSort(ResponseBuilder rb, SolrParams params) {
        Query query = rb.getQuery();
        FeatureExtractor extractor = extractorFactory.create(params, query);
        Classifier classifier = classifierFactory.createClassifier();
        FieldComparatorSource comparator = new RankingComparatorSource(
                extractor, classifier, params, query);
        Sort sort = new Sort(new SortField("ranking", comparator));
        return sort;
    }

    private void setRescoredResults(ResponseBuilder rb, TopDocs topDocs,
            int offset, int len) {
        DocListAndSet results = rb.getResults();
        int totalHits = results.docList.matches();
        int[] docs = new int[topDocs.scoreDocs.length];
        float[] scores = new float[topDocs.scoreDocs.length];
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            docs[i] = topDocs.scoreDocs[i].doc;
            scores[i] = topDocs.scoreDocs[i].score;
        }
        results.docList = new DocSlice(offset, len, docs, scores, totalHits,
                topDocs.getMaxScore());
        ResultContext ctx = (ResultContext) rb.rsp.getValues().get("response");
        ctx.docs = results.docList;
    }
}
