package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirProcess {

    public static List<File> walkJavaFile(String path){
        List<File> listJavaFile = new ArrayList<File>();
        return walkJavaFileRecursive(path, listJavaFile);
    }

    private static List<File> walkJavaFileRecursive(String path, List<File> listJavaFile) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) return listJavaFile;

        for (File f : list) {
            if (f.isDirectory()) {
                walkJavaFileRecursive(f.getAbsolutePath(), listJavaFile);
//                System.out.println("Dir:" + f.getAbsoluteFile());
            } else {
//                System.out.println("File:" + f.getAbsoluteFile());
                if (f.getName().toLowerCase().endsWith(".java")) {
                    listJavaFile.add(f);
                }
            }
        }
        return listJavaFile;
    }



    public static void main(String args[]) {
        List<File> list = walkJavaFile("/Users/maytinhdibo/Project/bomberman/");
        System.out.println(list.size());
    }

}
