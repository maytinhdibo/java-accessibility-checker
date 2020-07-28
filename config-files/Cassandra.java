package config;

public class Config {
    public static String STORAGE_DIR = "/Users/maytinhdibo/Project/java-accessibility-checker/storage/";

    public static String PROJECT_DIR = "/Users/maytinhdibo/Downloads/data/cassandra/src";
    public static String[] SOURCE_PATH = {
            "/Users/maytinhdibo/Downloads/data/cassandra/src/java",
            "/Users/maytinhdibo/Downloads/data/cassandra/interface/thrift/gen-java/"
    };

    public static String[] ENCODE_SOURCE = {"utf-8", "utf-8"};


    public static String[] CLASS_PATH = {
            "/Users/maytinhdibo/Downloads/data/cassandra/lib/antlr-3.2.jar",
            "/Users/maytinhdibo/Downloads/data/cassandra/lib/commons-cli-1.1.jar",
            "/Users/maytinhdibo/Downloads/data/cassandra/lib/libthrift-0.5.jar",
            "/Users/maytinhdibo/Downloads/data/cassandra/lib/jline-0.9.94.jar"
    };

    public static String TEST_FILE_PATH = "/Users/maytinhdibo/Downloads/data/cassandra/src/java/org/apache/cassandra/cli/CliMain.java";
    public static int TEST_POSITION = 1517;

    public static String LOG_DIR = "/Users/maytinhdibo/Project/java-accessibility-checker/storage/output/";
}
