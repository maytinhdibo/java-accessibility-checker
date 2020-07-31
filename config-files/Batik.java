package config;

import java.io.File;

public class Config {
    public static String STORAGE_DIR = "storage/";

    public static String PROJECT_NAME = "ant";

    public static String PROJECT_DIR = "../../Research/java-data/batik/sources/";
    public static String[] SOURCE_PATH = {
            "../../Research/java-data/batik/sources/",
    };

    public static String[] ENCODE_SOURCE = {"utf-8"};


    public static String[] CLASS_PATH = {
            "../../Research/java-data/batik/lib/js.jar",
            "../../Research/java-data/batik/lib/xerces_2_5_0.jar",
            "../../Research/java-data/batik/lib/pdf-transcoder.jar",
            "../../Research/java-data/batik/lib/xalan-2.6.0.jar",
            "../../Research/java-data/batik/lib/build/crimson-1.1.3.jar",
            "../../Research/java-data/batik/lib/build/ant-1.6.5.jar",
            "../../Research/java-data/batik/lib/build/ant-launcher-1.6.5.jar",
            "../../Research/java-data/batik/lib/xml-apis-ext.jar",
            "../../Research/java-data/batik/lib/xml-apis.jar",
            "../../Research/java-data/lib/batik/jython-standalone-2.7.2rc1.jar"
    };

    public static int JDT_LEVEL = 8;
    public static String JAVA_VERSION = "1.8";

    public static File[] IGNORE_FILES = new File[]{
            new File("../../Research/java-data/batik/sources/org/apache/batik/ext/awt/image/codec/tiff/TIFFEncodeParam.java"),
            new File("../../Research/java-data/batik/sources/org/apache/batik/ext/awt/image/codec/tiff/TIFFImageEncoder.java"),
            new File("../../Research/java-data/batik/sources/org/apache/batik/ext/awt/image/codec/tiff/TIFFImage.java"),
            new File("../../Research/java-data/batik/sources/org/apache/batik/script/jpython/JPythonInterpreterFactory.java"),
            new File("../../Research/java-data/batik/sources/org/apache/batik/script/jacl/JaclInterpreter.java"),
            new File("../../Research/java-data/batik/sources/org/apache/batik/script/jacl/JaclInterpreterFactory.java")
    };

    public static String TEST_FILE_PATH = "../../Research/java-data/batik/sources/org/apache/batik/apps/rasterizer/SVGConverter.java";
    public static int TEST_POSITION = 1662;

    public static String LOG_DIR = "storage/output/";
}
