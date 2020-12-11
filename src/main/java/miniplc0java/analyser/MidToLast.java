package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class MidToLast {
//    测试集
//    public static void main(String[] args) {
//        ArrayList<ExpressionToken> tokens = new ArrayList<>();
//        tokens.add(new ExpressionToken(ExpressionType.NEG));
//        tokens.add(new ExpressionToken(ExpressionType.FUNC));
//        tokens.add(new ExpressionToken(ExpressionType.PLUS));
//        tokens.add(new ExpressionToken(ExpressionType.UINT_LITERAL));
//        tokens.add(new ExpressionToken(ExpressionType.MUL));
//        tokens.add(new ExpressionToken(ExpressionType.Var));
//        tokens.add(new ExpressionToken(ExpressionType.LE));
//        tokens.add(new ExpressionToken(ExpressionType.L_PAREN));
//        tokens.add(new ExpressionToken(ExpressionType.Var));
//        tokens.add(new ExpressionToken(ExpressionType.MUL));
//        tokens.add(new ExpressionToken(ExpressionType.FUNC));
//        tokens.add(new ExpressionToken(ExpressionType.MINUS));
//        tokens.add(new ExpressionToken(ExpressionType.FUNC));
//        tokens.add(new ExpressionToken(ExpressionType.R_PAREN));
//        tokens.add(new ExpressionToken(ExpressionType.MINUS));
//        tokens.add(new ExpressionToken(ExpressionType.Var));
//        ArrayList<ExpressionToken> tokens1 = midToLast(tokens);
//        for (ExpressionToken i : tokens1) {
//            System.out.print(i.type+" ");
//        }
//    }

    public static ArrayList<ExpressionToken> midToLast(ArrayList<ExpressionToken> input)  throws AnalyzeError{
        //优先级矩阵
        priority priority = new priority();
        Map<ExpressionType, Integer> map = priority.map;
        //输出表达式列表
        ArrayList<ExpressionToken> output = new ArrayList<ExpressionToken>();
        //操作符栈
        Stack<ExpressionToken> operation = new Stack<ExpressionToken>();
        //输入指针
        int inputP = 0;
        while (operation.size() != 0 || inputP != input.size()) {
            //如果操作符栈中还有操作数
            if (inputP == input.size() && (operation.size() > 0)) {
                while (operation.size() > 0) {
                    ExpressionToken tmp = operation.pop();
                    output.add(tmp);
                }
                break;
            }
            //如果是操作数
            if (input.get(inputP).type == ExpressionType.Var || input.get(inputP).type == ExpressionType.FUNC || input.get(inputP).type == ExpressionType.UINT_LITERAL) {
                output.add(input.get(inputP));
                inputP++;
            }
            //如果不是操作数
            else {
                //如果是右括号，一直弹出直到遇见左括号
                if (input.get(inputP).type == ExpressionType.R_PAREN) {
                    while (operation.peek().type != ExpressionType.L_PAREN) {
                        output.add(operation.pop());
                    }
                    //弹出左括号
                    operation.pop();
                    //去掉右括号
                    inputP++;
                }
                //如果操作栈不为0，且不是左括号，遇到 输入的优先级<=符号栈中的 就弹栈
                else {
                    while ((operation.size() > 0) && (operation.peek().type != ExpressionType.L_PAREN) && (map.get(input.get(inputP).type) <= map.get(operation.peek().type))) {
                        //如果是neg且==则不弹
                        if ((map.get(input.get(inputP).type) == map.get(operation.peek().type)) && operation.peek().type == ExpressionType.NEG) {
                            break;
                        }
                        output.add(operation.pop());
                    }
                    operation.add(input.get(inputP));
                    inputP++;
                }
            }
        }
        return output;
    }
}

class priority {
    Map<ExpressionType, Integer> map;

    priority() {
        map = new HashMap<ExpressionType, Integer>();
        map.put(ExpressionType.PLUS, 1);
        map.put(ExpressionType.NEG, 3);
        map.put(ExpressionType.MINUS, 1);
        map.put(ExpressionType.MUL, 2);
        map.put(ExpressionType.DIV, 2);
        map.put(ExpressionType.L_PAREN, 4);
        map.put(ExpressionType.LT, 0);
        map.put(ExpressionType.LE, 0);
        map.put(ExpressionType.NEQ, 0);
        map.put(ExpressionType.EQ, 0);
        map.put(ExpressionType.GE, 0);
        map.put(ExpressionType.GT, 0);
    }
}