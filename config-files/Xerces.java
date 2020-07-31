package config;

import java.io.File;

public class Config {
    public static String STORAGE_DIR = "storage/";

    public static String PROJECT_DIR = "../../Research/java-data/xerces/src/";
    public static String[] SOURCE_PATH = {
            "../../Research/java-data/xerces/src/",
    };

    public static String[] ENCODE_SOURCE = {"utf-8"};


    public static String[] CLASS_PATH = {
            "../../Research/java-data/xerces/tools/resolver.jar",
            "../../Research/java-data/xerces/tools/xml-apis.jar"
    };

    public static int JDT_LEVEL = 8;
    public static String JAVA_VERSION = "1.8";

    public static File[] IGNORE_FILES = new File[]{
//            new File("../../Research/java-data/batik/sources/org/apache/batik/ext/awt/image/codec/tiff/TIFFEncodeParam.java"),
    };

    public static String TEST_FILE_PATH = "../../Research/java-data/xerces/src/org/apache/xerces/dom/CDATASectionImpl.java";
    public static int TEST_POSITION = 1662;

    public static String LOG_DIR = "storage/output/";
}
