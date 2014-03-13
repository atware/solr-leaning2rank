package solr.learning2rank;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.common.util.NamedList;
import org.junit.BeforeClass;
import org.junit.Test;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.feature.FeatureExtractor;
import solr.learning2rank.feature.RankingFeature;

public class RankingComparatorTest {

    private static Directory directory;
    private final FeatureExtractor extractor = new TestFeatureExtractor();
    private final Classifier classifier = new TestClassifier();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        directory = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,
                analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        addDocument(indexWriter, "10");
        addDocument(indexWriter, "1");
        addDocument(indexWriter, "9");
        addDocument(indexWriter, "3");
        addDocument(indexWriter, "7");
        addDocument(indexWriter, "6");
        addDocument(indexWriter, "2");
        addDocument(indexWriter, "8");
        addDocument(indexWriter, "4");
        addDocument(indexWriter, "5");
        indexWriter.close();
    }

    @Test
    public void test() throws IOException {

        IndexSearcher searcher = searcher();
        Query query = new MatchAllDocsQuery();
        TopDocs topDocs = searcher.search(query, 5, getRaningSort(query));

        assertThat(topDocs.scoreDocs.length, is(5));
        assertThat(getId(topDocs.scoreDocs[0].doc, searcher), is("1"));
        assertThat(getId(topDocs.scoreDocs[1].doc, searcher), is("2"));
        assertThat(getId(topDocs.scoreDocs[2].doc, searcher), is("3"));
        assertThat(getId(topDocs.scoreDocs[3].doc, searcher), is("4"));
        assertThat(getId(topDocs.scoreDocs[4].doc, searcher), is("5"));
    }

    private static void addDocument(IndexWriter indexWriter, String id) throws IOException {
        Document document = new Document();
        document.add(new StringField("id", id, Store.YES));
        indexWriter.addDocument(document);
    }

    private IndexSearcher searcher() throws IOException {
        return new IndexSearcher(DirectoryReader.open(directory));
    }

    private Sort getRaningSort(Query query) {
        FieldComparatorSource comparator = new RankingComparatorSource(
                extractor, classifier, null, query);
        Sort sort = new Sort(new SortField[] { new SortField("ranking",
                comparator) });
        return sort;
    }

    private String getId(int doc, IndexSearcher searcher) throws IOException {
        return searcher.getIndexReader().document(doc).getField("id")
                .stringValue();
    }

    public class TestClassifier implements Classifier {

        @Override
        public void init(NamedList<?> initParams) {
        }

        @Override
        public double getScore(RankingFeature[] target) {
            double score = 0;
            for (RankingFeature feature : target) {
                score += feature.getValue();
            }
            return score;
        }

    }

    public class TestFeatureExtractor implements FeatureExtractor {

        private AtomicReader reader;
        private Set<String> fields;

        public TestFeatureExtractor() {
            fields = new HashSet<String>();
            fields.add("id");
        }

        @Override
        public void setNextReader(AtomicReaderContext context)
                throws IOException {
            reader = context.reader();
        }

        @Override
        public RankingFeature[] extract(int docNum) throws IOException {
            Document document = reader.document(docNum, fields);
            String id = document.getField("id").stringValue();
            RankingFeature feature = new RankingFeature(0,
                    Double.parseDouble(id));
            return new RankingFeature[] { feature };
        }
    }
}
