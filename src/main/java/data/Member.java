package data;

import org.eclipse.jdt.core.dom.IBinding;

import java.util.List;

public class Member {
    private IBinding member;

    public Member(IBinding member) {
        this.member = member;
    }

    public IBinding getMember() {
        return member;
    }

    public void setMember(IBinding member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return member.toString();
    }
}
