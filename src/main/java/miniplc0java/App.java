package miniplc0java;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.Function;
import miniplc0java.analyser.StackVar;
import miniplc0java.error.CompileError;
import miniplc0java.instruction.Instruction;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;

import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class App {
    public static void main(String[] args) throws CompileError {
        var argparse = buildArgparse();
        Namespace result;
        try {
            result = argparse.parseArgs(args);
        } catch (ArgumentParserException e1) {
            argparse.handleError(e1);
            return;
        }

        var inputFileName = result.getString("input");
        var outputFileName = result.getString("output");

        InputStream input;
        if (inputFileName.equals("-")) {
            input = System.in;
        } else {
            try {
                input = new FileInputStream(inputFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find input file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        PrintStream output;
        if (outputFileName.equals("-")) {
            output = System.out;
        } else {
            try {
                output = new PrintStream(new FileOutputStream(outputFileName));
            } catch (FileNotFoundException e) {
                System.err.println("Cannot open output file.");
                e.printStackTrace();
                System.exit(2);
                return;
            }
        }

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = tokenize(iter);

        if (result.getBoolean("tokenize")) {
            // tokenize
            var tokens = new ArrayList<Token>();
            try {
                while (true) {
                    var token = tokenizer.nextToken();
                    if (token.getTokenType().equals(TokenType.EOF)) {
                        break;
                    }
                    if (token.getTokenType().equals(TokenType.COMMENT)) {
                        continue;
                    }
                    tokens.add(token);
                }
            } catch (Exception e) {
                // 遇到错误不输出，直接退出
                System.err.println(e);
                System.exit(0);
                return;
            }
            for (Token token : tokens) {
                output.println(token.toString());
            }
        } else if (result.getBoolean("analyse")) {
            // analyze
            var analyzer = new Analyser(tokenizer);
            List<Instruction> instructions;
            try {
                instructions = analyzer.analyse();
            } catch (Exception e) {
                // 遇到错误不输出，直接退出
                System.err.println(e);
                System.exit(1);
                return;
            }
            //----------------------------------

//            output.println("72303b3e");
//            output.println("00000001");
            System.out.println(String.format("%08x", analyzer.globalP + 1));
            for (int i = 0; i <= analyzer.globalP; i++) {//输出全局变量
                StackVar tmp = analyzer.globalVar[i];
                if (tmp.isConst)
                    System.out.println("01");
                else {
                    System.out.println("00");
                }
                if (tmp.isFunction) {
                    String name = tmp.name;
                    System.out.println(String.format("%08x", name.length()));
                    for (int j = 0; j < name.length(); j++) {
                        StringBuilder str = new StringBuilder();
                        char ch=name.charAt(j);
                        if(ch == '\\') {
                            j++;
                            ch = name.charAt(j);
                            if (ch == 'n') {
                                ch = '\n';
                            } else if (ch == '\\') {
                                ch = '\\';
                            } else if (ch == 't') {
                                ch = '\t';
                            } else if (ch == 'r') {
                                ch = '\r';
                            }
                        }
                        str.append(ch);
                        System.out.print(str);
                    }
                    System.out.print('\n');
                } else {
                    System.out.println("00000008");
                    System.out.println("0000000000000000");
                }
            }
            System.out.println(String.format("%08x", analyzer.funcP + 1));
            for (int i = 0; i <= analyzer.funcP; i++) {
                Function tmp = analyzer.funcList[i];
//                output.println(String.format("%08x", tmp.global_num));
//                if(tmp.return_num!=3){
//                    output.println(String.format("%08x", 1));
//                }else{
//                    output.println(String.format("%08x", 0));
//                }
//                output.println(String.format("%08x", tmp.args_num));
//                output.println(String.format("%08x", tmp.locals_num));
//                output.println(String.format("%08x", tmp.getOperations().size()));
                System.out.println("("+tmp.funcNum+")");
                StringBuilder str=new StringBuilder().append("fn [").append(tmp.globalNum).append("] ")
                        .append(tmp.localNum).append(" ").append(tmp.paramNum).append(" -> ");
                if(tmp.returnType!=2){
                    str.append(1);
                }else{
                    str.append(0);
                }
                System.out.println(str+" {");
                ArrayList<Instruction> ops=tmp.getOperations();
                for(int j=0;j<ops.size();j++){
                    System.out.println(String.format("     %d:%s",j,ops.get(j)));
                }
                System.out.println("}");
            }
            DataOutputStream out = null;
            try {
                out = new DataOutputStream(new FileOutputStream(new File(outputFileName)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            PrintToBinary p_to_byte = new PrintToBinary();
            List<Byte> tmp_byte_list = p_to_byte.output_print(analyzer);
            byte[] tmp_byte = new byte[tmp_byte_list.size()];
            for(int i = 0; i < tmp_byte_list.size(); i++)
                tmp_byte[i] = tmp_byte_list.get(i);
            try {
                assert out != null;
                out.write(tmp_byte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Please specify either '--analyse' or '--tokenize'.");
            System.exit(3);
        }
    }

    private static ArgumentParser buildArgparse() {
        var builder = ArgumentParsers.newFor("miniplc0-java");
        var parser = builder.build();
        parser.addArgument("-t", "--tokenize").help("Tokenize the input").action(Arguments.storeTrue());
        parser.addArgument("-l", "--analyse").help("Analyze the input").action(Arguments.storeTrue());
        parser.addArgument("-o", "--output").help("Set the output file").required(true).dest("output")
                .action(Arguments.store());
        parser.addArgument("file").required(true).dest("input").action(Arguments.store()).help("Input file");
        return parser;
    }

    private static Tokenizer tokenize(StringIter iter) {
        var tokenizer = new Tokenizer(iter);
        return tokenizer;
    }
}
