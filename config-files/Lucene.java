package config;

public class Config {
    public static String STORAGE_DIR = "storage/";

    public static String PROJECT_DIR = "../../Research/java-data/lucene/lucene/src/java/";
    public static String[] SOURCE_PATH = {
            "../../Research/java-data/lucene/lucene/src/java/",
    };

    public static String[] ENCODE_SOURCE = {"utf-8"};


    public static String[] CLASS_PATH = {
//            "../../Research/java-data/lucene/lucene/lib/servlet-api-2.4.jar"
    };

    public static String TEST_FILE_PATH = "../../Research/java-data/lucene/lucene/src/java/org/apache/lucene/document/AbstractField.java";
    public static int TEST_POSITION = 1662;

    public static File[] IGNORE_FILES = new File[]{};

    public int JDT_LEVEL = 13;
    public String JAVA_VERSION = "13";

    public static String LOG_DIR = "storage/output/";
}
