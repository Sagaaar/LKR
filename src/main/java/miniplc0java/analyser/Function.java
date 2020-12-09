package miniplc0java.analyser;

public class Function {
    //参数个数
    int paramNum;
    //该函数在全局变量的位置
    int globalNum;
    //局部变量个数
    int localNum;
    //return的函数类型
    int returnType;
    //在函数列表里的位置
    int funcNum;
    //是不是内置函数
    boolean isInner;

    public Function(int paramNum, int globalNum, int localNum, int returnType, int funcNum, boolean isInner) {
        this.paramNum = paramNum;
        this.globalNum = globalNum;
        this.localNum = localNum;
        this.returnType = returnType;
        this.funcNum = funcNum;
        this.isInner = isInner;
    }
}
