package miniplc0java.instruction;

import miniplc0java.analyser.StackVar;

import java.util.Objects;

public class Instruction {
    public Operation opt;
    public int arg1;
    public long arg2;
    public boolean flag=false;

    public Instruction(Operation opt) {
        this.opt = opt;
    }

    public Instruction(Operation opt, int x) {
        this.opt = opt;
        arg1=x;
        flag=true;
    }

    public Instruction(Operation opt, long x) {
        this.opt = opt;
        arg2=x;
        flag=true;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o)
//            return true;
//        if (o == null || getClass() != o.getClass())
//            return false;
//        Instruction that = (Instruction) o;
//        return opt == that.opt && Objects.equals(x, that.x);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(opt, x);
//    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public void setArg1(int arg1){this.arg1=arg1;}

    //    @Override
//    public String toString() {
//        switch (this.opt) {
//            case not:
//                return "2e";
//            case neg_i:
//                return "34";
//            case nop:
//                return "00";
//            case push:
//                return String.format("01%016x",this.arg2);
//            case pop:
//                return "02";
//            case popn:
//                return String.format("03%08x",this.arg1);
//            case dup:
//                return "04";
//            case loca:
//                return String.format("0a%08x",this.arg1);
//            case arga:
//                return String.format("0b%08x",this.arg1);
//            case globa:
//                return String.format("0c%08x",this.arg1);
//            case load_8:
//                return "10";
//            case load_16:
//                return "11";
//            case load_32:
//                return "12";
//            case load_64:
//                return "13";
//            case store_8:
//                return "14";
//            case store_16:
//                return "15";
//            case store_32:
//                return "16";
//            case store_64:
//                return "17";
//            case stackalloc:
//                return String.format("1a%08x",this.arg1);
//            case add_i:
//                return "20";
//            case sub_i:
//                return "21";
//            case mul_i:
//                return "22";
//            case div_i:
//                return "23";
//            case cmp_i:
//                return "30";
//            case set_lt:
//                return "39";
//            case set_gt:
//                return "3a";
//            case br:
//                return String.format("41%08x",this.arg1);
//            case br_false:
//                return String.format("42%08x",this.arg1);
//            case br_true:
//                return String.format("43%08x",this.arg1);
//            case call:
//                return String.format("48%08x",this.arg1);
//            case ret:
//                return "49";
//            case callname:
//                return String.format("4a%08x",this.arg1);
//            default:
//                return "fe";
//        }
//    }
    @Override
    public String toString() {
        switch (this.opt) {
            case not:
                return "Not";
            case neg_i:
                return "NegI";
            case nop:
                return "Nop()";
            case push:
                return String.format("Push(%s)",this.arg2);
            case pop:
                return "Pop()";
            case popn:
                return String.format("PopN(%s)",this.arg1);
            case dup:
                return "Dup()";
            case loca:
                return String.format("LocA(%s)",this.arg1);
            case arga:
                return String.format("ArgA(%s)",this.arg1);
            case globa:
                return String.format("GlobA(%s)",this.arg1);
            case load_8:
                return "Load8";
            case load_16:
                return "Load16";
            case load_32:
                return "Load32";
            case load_64:
                return "Load64";
            case store_8:
                return "Store8";
            case store_16:
                return "Store16";
            case store_32:
                return "Store32";
            case store_64:
                return "Store64";
            case stackalloc:
                return String.format("StackAlloc(%s)",this.arg1);
            case add_i:
                return "AddI";
            case sub_i:
                return "SubI";
            case mul_i:
                return "MulI";
            case div_i:
                return "DivI";
            case cmp_i:
                return "CmpI";
            case set_lt:
                return "SetLt";
            case set_gt:
                return "SetGt";
            case br:
                return String.format("Br(%s)",this.arg1);
            case br_false:
                return String.format("BrFalse(%s)",this.arg1);
            case br_true:
                return String.format("BrTrue(%s)",this.arg1);
            case call:
                return String.format("Call(%s)",this.arg1);
            case ret:
                return "Ret";
            case callname:
                return String.format("CallName(%s)",this.arg1);
            default:
                return "Panic";
        }
    }
}
