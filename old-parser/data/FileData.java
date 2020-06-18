package data;

public class FileData {
    public String path;
    public String text;

    public FileData(String path) {
        this.path = path;
    }

    public FileData(String path, String text) {
        this.path = path;
        this.text = text;
    }
}

