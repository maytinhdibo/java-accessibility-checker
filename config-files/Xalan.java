package config;

public class Config {
    public static String STORAGE_DIR = "storage/";

    public static String PROJECT_DIR = "../../Research/java-data/xalan/src/";
    public static String[] SOURCE_PATH = {
            "../../Research/java-data/xalan/src/",
    };

    public static String[] ENCODE_SOURCE = {"utf-8"};


    public static String[] CLASS_PATH = {
            "../../Research/java-data/xalan/lib/BCEL.jar",
            "../../Research/java-data/xalan/lib/regexp.jar",
            "../../Research/java-data/xalan/lib/runtime.jar",
            "../../Research/java-data/xalan/lib/xercesImpl.jar",
            "../../Research/java-data/lib/xalan/java-cup-11b-runtime.jar",
            "../../Research/java-data/lib/xalan/java-cup-11b.jar"
    };

    public static String TEST_FILE_PATH = "../../Research/java-data/xalan/src/org/apache/xalan/lib/ExsltBase.java";
    public static int TEST_POSITION = 1662;

    public static File[] IGNORE_FILES = new File[]{};

    public int JDT_LEVEL = 13;
    public String JAVA_VERSION = "13";

    public static String LOG_DIR = "storage/output/";
}
