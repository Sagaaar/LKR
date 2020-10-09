import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Analysis {
    public static String[] reserved = {"BEGIN", "END", "FOR", "IF", "THEN", "ELSE"};
    public static String[] reservedOutput = {"Begin", "End", "For", "If", "Then", "Else"};
    public static StringBuffer token = new StringBuffer();
    public static int c;
    public static char word;
    public static char buffer;//存储回退的字符
    //main
    public static void main(String[] args) throws IOException {
//        //从本地文件中读取测试
//        File file = new File("C:\\Users\\40535\\Desktop\\大三下学期\\编译原理\\作业\\test.txt");
//        BufferedReader input = new BufferedReader(new FileReader(file));

        //正式
        String name = args[0];
        BufferedReader input = new BufferedReader(new FileReader(name));
        //getChar
        c = input.read();
        word = (char)c;
        buffer = word;
        while(true){
            token.setLength(0);
            //跳过空格、换行、Tab读取下一个字符
            while(isSpace(word) || isNewLine(word) || isTab(word) || isEnter(word)){
                //getChar
                c = input.read();
                //文件末尾
                if(c == -1){
                    break;
                }
                word = (char)c;
                buffer = word;
            }
            if(c == -1){
                break;
            }
            //判断当前字符是否是字母开头
            if(isLetter(word)){
                while(isLetter(word) || isDigital(word)){
                    catToken(word);
                    //getChar
                    c = input.read();
                    if(c == -1){
                        break;
                    }
                    word = (char)c;
                    buffer = word;
                }
                if(c == -1){
                    break;
                }
                retract(word);
                int resultValue = reserver();
                //标识符
                if(resultValue == -1){
                    System.out.println("Ident(" + token.toString() + ")");
                }
                //保留字
                else {
                    System.out.println(reservedOutput[resultValue]);
                }
                continue;
            }
            //判断当前字符是否是数字开头
            else if(isDigital(word)){
                while(isDigital(word)){
                    catToken(word);
                    //getChar
                    c = input.read();
                    if(c == -1){
                        break;
                    }
                    word = (char)c;
                    buffer = word;
                }
                if(c == -1){
                    break;
                }
                retract(word);
                int num = transNum(token);
                System.out.println("Int(" + num + ")");
                continue;
            }
            else if(isColon(word)){
                //getChar
                c = input.read();
                if(c == -1){
                    break;
                }
                word = (char)c;
                buffer = word;
                //判断是否是赋值符号
                if(isEqu(word)){
                    System.out.println("Assign");
                }
                else{
                    System.out.println("Colon");
                }
                continue;
            }
            //判断是否是加号
            else if(isPlus(word)){
                System.out.println("Plus");
            }
            //判断是否是星号
            else if(isStar(word)){
                System.out.println("Star");
            }
            //判断是否是逗号
            else if(isComma(word)){
                System.out.println("Comma");
            }
            //判断是否是左括号
            else if(isLpar(word)){
                System.out.println("LParenthesis");
            }
            //判断是否是右括号
            else if(isRpar(word)){
                System.out.println("RParenthesis");
            }
            else{
                System.out.println("Unknown");
            }
            c = input.read();
            if(c == -1){
                break;
            }
            word = (char)c;
            buffer = word;
        }
    }
    //字符转换成数字
    public static int transNum(StringBuffer a){
        int len = a.length();
        int res = 0;
        int jud = 0;
        int i = 0;
        for(i = 0; i  < len; i++){
            if(a.charAt(i) != 0){
                jud = 1;
                break;
            }
        }
        if(i == len) return 0;
        else {
            token.delete(0, i);
            return Integer.valueOf(Integer.parseInt(token.toString()));
        }
    }
    //查找保留字的返回值
    public static int reserver(){
        for(int i = 0; i < 6; i++){
            if(token.toString().equals(reserved[i])){
                return i;
            }
        }
        return -1;
    }
    //回退
    public static void retract(char a){
        word = buffer;
    }
    //追加token
    public static void catToken(char a){
        token.append(a);
    }
    //判断是否是空格
    public static boolean isSpace(char a){
        return a == 32;
    }
    //判断是否是换行
    public static boolean isNewLine(char a){
        return a == 10;
    }
    //判断是否是回车
    public static boolean isEnter(char a){
        return a == 13;
    }
    //判断是否是Tab
    public static boolean isTab(char a){
        return a == 9;
    }
    //判断是否是字母
    public static boolean isLetter(char a){
        if( (a >= 65 && a <= 90) || (a >= 97 && a <= 122)){
            return true;
        }
        return false;
    }
    //判断是否是数字
    public static boolean isDigital(char a){
        if(a >= 48 && a <= 57){
            return true;
        }
        return false;
    }
    //判断是否是右括号
    private static boolean isRpar(char a) {
        return a == 41;
    }
    //判断是否是左括号
    private static boolean isLpar(char a) {
        return a == 40;
    }
    //判断是否是逗号
    private static boolean isComma(char a) {
        return a == 44;
    }
    //判断是否是星号
    private static boolean isStar(char a) {
        return a == 42;
    }
    //判断是否是加号
    private static boolean isPlus(char a) {
        return a == 43;
    }
    //判断是否是赋值符号
    private static boolean isEqu(char a) {
        return a == 61;
    }
    //判断是否是冒号
    private static boolean isColon(char a) {
        return a == 58;
    }
}
