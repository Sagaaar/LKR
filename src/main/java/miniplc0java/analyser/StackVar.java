package miniplc0java.analyser;

public class StackVar {
    //是否是变量
    public Boolean isConst;
    //是否初始化
    public Boolean isInitialized;
    //是否是函数
    public Boolean isFunction;
    //标识符名字
    public String name;
    //标识符类型 unknown:0 int:1 void:2 double:3
    public int type;
    //在全局/局部/参数中的位置
    public TypeAndPos typeAndPos;

    public StackVar(Boolean isConst, Boolean isInitialized, Boolean isFunction, String name, int type, TypeAndPos typeAndPos) {
        this.isConst = isConst;
        this.isInitialized = isInitialized;
        this.isFunction =isFunction;
        this.name = name;
        this.type = type;
        this.typeAndPos = typeAndPos;
    }
}
