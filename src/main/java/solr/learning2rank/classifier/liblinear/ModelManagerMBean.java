package solr.learning2rank.classifier.liblinear;


/**
 * Liblinearのモデルとスケーラーの取得・更新操作用のMBeanです.
 * 
 * @author atwkwbr
 * 
 */
public interface ModelManagerMBean {

    public void setModel(LiblinearModel modelAndScaler);

    public LiblinearModel getModel();
}
