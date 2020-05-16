package data;

public class ParseFrom {
    public boolean inMember = false;
    public String classId;

    public ParseFrom(boolean inMember) {
        this.inMember = inMember;
    }

    public ParseFrom(String classId) {
        this.classId = classId;
    }

    @Override
    public String toString() {
        return inMember?"This member":classId;
    }
}
