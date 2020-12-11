package miniplc0java.analyser;

import java.util.ArrayList;

public class ExpressionToken {
    ExpressionType type;
    public ExpressionToken(ExpressionType type) {
        this.type = type;
    }
}

//常量
class Uinteger extends ExpressionToken{
    long value;

    public Uinteger(ExpressionType type, long value) {
        super(type);
        this.value = value;
    }
}

//函数名
class callFunc extends ExpressionToken{
    ArrayList<ArrayList<ExpressionToken>> param = new ArrayList<ArrayList<ExpressionToken>>();
    Function function;

    public callFunc(ExpressionType type, Function function) {
        super(type);
        this.function = function;
    }

    public void addParam(ArrayList<ExpressionToken> param){
        this.param.add(param);
    }
}

//变量
class Var extends ExpressionToken{
    StackVar varValue;

    public Var(ExpressionType type, StackVar varValue) {
        super(type);
        this.varValue = varValue;
    }
}
