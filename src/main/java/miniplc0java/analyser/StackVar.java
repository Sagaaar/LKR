package miniplc0java.analyser;

public class StackVar {
    //是否是变量
    Boolean isConst;
    //是否初始化
    Boolean isInitialized;
    //标识符名字
    String name;
    //标识符类型 unknown:0 int:1 void:2 double:3
    int type;
    //在全局/局部/参数中的位置
    TypeAndPos typeAndPos;

    public StackVar(Boolean isConst, Boolean isInitialized, String name, int type, TypeAndPos typeAndPos) {
        this.isConst = isConst;
        this.isInitialized = isInitialized;
        this.name = name;
        this.type = type;
        this.typeAndPos = typeAndPos;
    }
}
