package miniplc0java.analyser;

import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;

import java.util.ArrayList;

public class Function {
    //参数个数
    public int paramNum;
    //该函数在全局变量的位置
    public int globalNum;
    //局部变量个数
    public int localNum;
    //return的函数类型
    public int returnType;
    //在函数列表里的位置
    public int funcNum;
    //是不是内置函数
    public boolean isInner;
    //操作列表
    ArrayList<Instruction> operations = new ArrayList<>();

    public Function(int paramNum, int globalNum, int localNum, int returnType, int funcNum, boolean isInner) {
        this.paramNum = paramNum;
        this.globalNum = globalNum;
        this.localNum = localNum;
        this.returnType = returnType;
        this.funcNum = funcNum;
        this.isInner = isInner;
    }

    public ArrayList<Instruction> getOperations(){
        return this.operations;
    }

    public void addOperations(Instruction operation){
        this.operations.add(operation);
    }
}
