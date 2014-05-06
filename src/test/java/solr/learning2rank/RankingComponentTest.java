package solr.learning2rank;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Query;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.junit.BeforeClass;
import org.junit.Test;

import solr.learning2rank.classifier.Classifier;
import solr.learning2rank.classifier.ClassifierFactory;
import solr.learning2rank.feature.FeatureExtractor;
import solr.learning2rank.feature.FeatureExtractorFactory;
import solr.learning2rank.feature.RankingFeature;

public class RankingComponentTest extends SolrTestCaseJ4 {

    static String rh = "rescore";

    static {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        initCore("solrconfig.xml", "schema.xml", "src/test/resources", "test");
    }

    private static final String OUT_OF_FIRST_SORT_RANGE = "100";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertU(addDoc("1", OUT_OF_FIRST_SORT_RANGE, "14"));
        assertU(addDoc("2", "1", "15"));
        assertU(addDoc("3", "2", "10"));
        assertU(addDoc("4", "3", "5"));
        assertU(addDoc("5", OUT_OF_FIRST_SORT_RANGE, "8"));
        assertU(addDoc("6", "4", "1"));
        assertU(addDoc("7", "5", "12"));
        assertU(addDoc("8", OUT_OF_FIRST_SORT_RANGE, "11"));
        assertU(addDoc("9", "6", "4"));
        assertU(addDoc("10", OUT_OF_FIRST_SORT_RANGE, "7"));
        assertU(addDoc("11", "7", "2"));
        assertU(addDoc("12", "8", "13"));
        assertU(addDoc("13", "9", "6"));
        assertU(addDoc("14", "10", "3"));
        assertU(addDoc("15", OUT_OF_FIRST_SORT_RANGE, "9"));
        assertU(commit());
    }

    private static String addDoc(String id, String firstSort, String secondSort) {
        return adoc("id", id, "first_sort", firstSort, "second_sort",
                secondSort);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        assertU(delQ("*:*"));
        optimize();
        assertU(commit());
    }

    @Test
    public void testNotRescore() {
        assertQ(req(CommonParams.QT, rh, //
                CommonParams.Q, "*:*", //
                CommonParams.START, "5", //
                CommonParams.ROWS, "5", //
                CommonParams.SORT, "first_sort asc", //
                CommonParams.FL, "id"), //
                "//result[@numFound='15' and @start='5']",
                "//result/doc[1]/str[@name='id' and text()='9']",
                "//result/doc[2]/str[@name='id' and text()='11']",
                "//result/doc[3]/str[@name='id' and text()='12']",
                "//result/doc[4]/str[@name='id' and text()='13']",
                "//result/doc[5]/str[@name='id' and text()='14']");
    }

    /**
     * first sort by first_sort value, and rescore by second_sort value
     */
    @Test
    public void testRescore() {
        assertQ(req(CommonParams.QT, rh, //
                CommonParams.Q, "*:*", //
                CommonParams.START, "0", //
                CommonParams.ROWS, "5", //
                CommonParams.SORT, "first_sort asc", //
                CommonParams.FL, "id", //
                "rescore", "true", //
                "first_pass_size", "10"), //
                "//result[@numFound='15' and @start='0']",
                "//result/doc[1]/str[@name='id' and text()='6']",
                "//result/doc[2]/str[@name='id' and text()='11']",
                "//result/doc[3]/str[@name='id' and text()='14']",
                "//result/doc[4]/str[@name='id' and text()='9']",
                "//result/doc[5]/str[@name='id' and text()='4']");

        assertQ(req(CommonParams.QT, rh, //
                CommonParams.Q, "*:*", //
                CommonParams.START, "5", //
                CommonParams.ROWS, "5", //
                CommonParams.SORT, "first_sort asc", //
                CommonParams.FL, "id", //
                "rescore", "true", //
                "first_pass_size", "10"), //
                "//result[@numFound='15' and @start='5']",
                "//result/doc[1]/str[@name='id' and text()='13']",
                "//result/doc[2]/str[@name='id' and text()='3']",
                "//result/doc[3]/str[@name='id' and text()='7']",
                "//result/doc[4]/str[@name='id' and text()='12']",
                "//result/doc[5]/str[@name='id' and text()='2']");
    }

    @Test
    public void testRescoreOutOfFirstSortSize() {
        assertQ(req(CommonParams.QT, rh, //
                CommonParams.Q, "*:*", //
                CommonParams.START, "8", //
                CommonParams.ROWS, "5", //
                CommonParams.SORT, "first_sort asc", //
                CommonParams.FL, "id", //
                "rescore", "true", //
                "first_pass_size", "5"), //
                "//result[@numFound='15' and @start='8']",
                "//result/doc[1]/str[@name='id' and text()='15']",
                "//result/doc[2]/str[@name='id' and text()='3']",
                "//result/doc[3]/str[@name='id' and text()='8']",
                "//result/doc[4]/str[@name='id' and text()='7']",
                "//result/doc[5]/str[@name='id' and text()='12']");
    }

    public static class TestClassifierFactory extends ClassifierFactory {

        @Override
        public void init(NamedList<?> initParams) {
        }

        @Override
        public Classifier createClassifier() {
            return new Classifier() {
                public void init(NamedList<?> initParams) {
                }

                @Override
                public double getScore(RankingFeature[] target) {
                    return target[0].getValue();
                }
            };
        }
    }

    public static class TestFeatureExtractorFactory extends
            FeatureExtractorFactory {

        private static final Set<String> fields = new HashSet<String>();

        static {
            fields.add("second_sort");
        }

        @Override
        public void init(NamedList<?> initParams) {
        }

        @Override
        public FeatureExtractor create(SolrParams params, Query query) {
            return new FeatureExtractor() {

                private AtomicReader reader;

                public void setNextReader(AtomicReaderContext context)
                        throws IOException {
                    reader = context.reader();
                }

                public RankingFeature[] extract(int docNum) throws IOException {
                    Document doc = reader.document(docNum, fields);
                    int vl = doc.getField("second_sort").numericValue()
                            .intValue();
                    return new RankingFeature[] { new RankingFeature(0, vl) };
                }
            };
        }
    }
}
