package miniplc0java.analyser;

public class StackPoint {
    //是否是函数栈指针
    Boolean isFn;
    //指向的位置
    int Value;

    public StackPoint(Boolean isFn, int value) {
        this.isFn = isFn;
        Value = value;
    }

}
