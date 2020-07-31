package config;

import java.io.File;

public class Config {
    public static String STORAGE_DIR = "storage/";

    public static String PROJECT_NAME = "ant";

    public static String PROJECT_DIR = "../../Research/java-data/ant/src/main/";
    public static String[] SOURCE_PATH = {
            "../../Research/java-data/ant/src/main/",
    };

    public static String[] ENCODE_SOURCE = {"utf-8"};


    public static String[] CLASS_PATH = {
            "../../Research/java-data/ant/lib/optional/junit-4.8.1.jar",
            "../../Research/java-data/ant/lib/optional/junit-3.8.2.jar",
            "../../Research/java-data/lib/ant/bcel-6.5.0/bcel-6.5.0.jar",
            "../../Research/java-data/lib/ant/jai-codec-1.1.3.jar",
            "../../Research/java-data/lib/ant/com.springsource.org.apache.oro-2.0.8.jar",
            "../../Research/java-data/lib/ant/jsch-0.1.46.jar",
            "../../Research/java-data/lib/ant/mail-1.4.7.jar",
            "../../Research/java-data/lib/ant/activation-1.1.1.jar",
            "../../Research/java-data/lib/ant/xalan-2.7.2.jar",
            "../../Research/java-data/lib/ant/jai_codec-1.1.3.jar",
            "../../Research/java-data/lib/ant/jdepend-2.9.1.jar",
            "../../Research/java-data/lib/ant/jai-core-1.1.3.jar",
            "../../Research/java-data/lib/ant/com.springsource.org.apache.log4j-1.2.16.jar",
            "../../Research/java-data/lib/ant/org.apache.xml.resolver-1.2.0-20081006.jar",
            "../../Research/java-data/lib/ant/commons-io-2.7.jar",
            "../../Research/java-data/lib/ant/apache-commons-net.jar",
            "../../Research/java-data/lib/ant/org-apache-commons-logging.jar",
            "../../Research/java-data/lib/ant/bsf-2.4.0.jar",
            "../../Research/java-data/lib/ant/com.springsource.com.ibm.netrexx-2.0.5.jar"
    };

    public static int JDT_LEVEL = 13;
    public static String JAVA_VERSION = "13";

    public static File[] IGNORE_FILES = new File[]{
//            new File("../../Research/java-data/batik/sources/org/apache/batik/ext/awt/image/codec/tiff/TIFFEncodeParam.java"),
    };

    public static String TEST_FILE_PATH = "../../Research/java-data/ant/src/main/org/apache/tools/mail/MailMessage.java";
    public static int TEST_POSITION = 1662;

    public static String LOG_DIR = "storage/output/";
}
