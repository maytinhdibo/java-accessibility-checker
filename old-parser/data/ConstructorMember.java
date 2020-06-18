package data;

import config.StringConstant;

public class ConstructorMember extends MemberWithParams {
    public ConstructorMember(String name, StringConstant accessLevel) {
        super(name, accessLevel);
    }
    public ConstructorMember(MemberWithParams memberWithParams){
        super(memberWithParams);
    }
}
