package config;

public class Config {
    public static String STORAGE_DIR = "storage/";

    public static String PROJECT_DIR = "../../Research/java-data/cassandra/src";
    public static String[] SOURCE_PATH = {
            "../../Research/java-data/cassandra/src/java",
            "../../Research/java-data/cassandra/interface/thrift/gen-java/"
    };

    public static String[] ENCODE_SOURCE = {"utf-8", "utf-8"};


    public static String[] CLASS_PATH = {
            "../../Research/java-data/cassandra/lib/avro-1.4.0-sources-fixes.jar",
            "../../Research/java-data/cassandra/lib/jackson-mapper-asl-1.4.0.jar",
            "../../Research/java-data/cassandra/lib/slf4j-api-1.6.1.jar",
            "../../Research/java-data/cassandra/lib/avro-1.4.0-fixes.jar",
            "../../Research/java-data/cassandra/lib/jline-0.9.94.jar",
            "../../Research/java-data/cassandra/lib/slf4j-log4j12-1.6.1.jar",
            "../../Research/java-data/cassandra/lib/commons-cli-1.1.jar",
            "../../Research/java-data/cassandra/lib/jug-2.0.0.jar",
            "../../Research/java-data/cassandra/lib/jackson-core-asl-1.4.0.jar",
            "../../Research/java-data/cassandra/lib/log4j-1.2.16.jar",
            "../../Research/java-data/cassandra/lib/snakeyaml-1.6.jar",
            "../../Research/java-data/cassandra/lib/json-simple-1.1.jar",
            "../../Research/java-data/cassandra/lib/jetty-util-6.1.24.jar",
            "../../Research/java-data/cassandra/lib/jetty-6.1.24.jar",
            "../../Research/java-data/cassandra/lib/concurrentlinkedhashmap-lru-1.1.jar",
            "../../Research/java-data/cassandra/lib/guava-r05.jar",
            "../../Research/java-data/cassandra/lib/libthrift-0.5.jar",
            "../../Research/java-data/cassandra/lib/antlr-3.2.jar",
            "../../Research/java-data/cassandra/lib/commons-codec-1.2.jar",
            "../../Research/java-data/cassandra/lib/commons-collections-3.2.1.jar",
            "../../Research/java-data/cassandra/lib/commons-lang-2.4.jar",
            "../../Research/java-data/cassandra/lib/high-scale-lib.jar",
            "../../Research/java-data/cassandra/lib/servlet-api-2.5-20081211.jar"
    };

    public static String TEST_FILE_PATH = "../../Research/java-data/cassandra/src/java/org/apache/cassandra/config/KSMetaData.java";
    public static int TEST_POSITION = 1662;

    public static String LOG_DIR = "storage/output/";
}
