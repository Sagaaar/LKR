package miniplc0java;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.StackVar;
import miniplc0java.analyser.SymbolEntry;
import miniplc0java.analyser.Function;
import miniplc0java.error.CompileError;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.StringIter;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;

public class PrintToBinary {

  public List<Byte> output_print(Analyser analyzer) {
    ArrayList<Byte> output_bytes = new ArrayList<>();
    //magic
    output_bytes.addAll(four_int_byte(4, 0x72303b3e));
    //version
    output_bytes.addAll(four_int_byte(4, 1));
    //globals.count
    output_bytes.addAll(four_int_byte(4, analyzer.leftP+1));
    //
    for(int i = 0; i <= analyzer.leftP; i++){
      StackVar tmp_symbol = analyzer.stackLeft[i];
      if(tmp_symbol.isConst)
        output_bytes.addAll(four_int_byte(1, 1));
      else
        output_bytes.addAll(four_int_byte(1, 0));
      //全局变量
      if(!tmp_symbol.isFunction){
        output_bytes.addAll(four_int_byte(4, 8));
        output_bytes.addAll(four_int_byte(8, 0));
      }
      //函数
      else{
        StackVar tmp_func = analyzer.stackLeft[i];
        assert tmp_func != null;
        String str=tmp_func.name;
        int length=0;
        for(int j=0;j<str.length();j++){
          if(str.charAt(j)=='\\'){
            j++;
          }
          length++;
        }
        output_bytes.addAll(four_int_byte(4, length));
        output_bytes.addAll(string_byte(tmp_func.name));
      }
    }

    output_bytes.addAll(four_int_byte(4, analyzer.funcP+1));

    for(int i = 0; i <= analyzer.funcP; i++){
      Function tmp_func = analyzer.funcList[i];
      //name
      output_bytes.addAll(four_int_byte(4, tmp_func.globalNum));
      //ret_slots
      if(tmp_func.returnType == 2)
        output_bytes.addAll(four_int_byte(4, 0));
      else
        output_bytes.addAll(four_int_byte(4, 1));
      //param_slots
      output_bytes.addAll(four_int_byte(4, tmp_func.paramNum));
      //loc_slots
      output_bytes.addAll(four_int_byte(4, tmp_func.localNum));
      //bodycount
      output_bytes.addAll(four_int_byte(4, tmp_func.getOperations().size()));
      //function_instruction
      for(int j = 0; j < tmp_func.getOperations().size(); j++) {
        Instruction tmp_inst = tmp_func.getOperations().get(j);
        output_bytes.addAll(instruction_byte(tmp_inst.getOpt()));
        if(tmp_inst.flag) {
          if (tmp_inst.opt == Operation.push) {
//            System.out.println(tmp_inst.arg2);
            output_bytes.addAll(four_int_byte(8, tmp_inst.arg2));
          } else {
            output_bytes.addAll(four_int_byte(4, tmp_inst.arg1));
          }
        }
      }
    }
    return output_bytes;
  }

//  public List<Byte> eight_long_byte(int length, long num){
//    ArrayList<Byte> bytes = new ArrayList<>();
//    int start = 8*(length-1);
//    for(int i = 0; i < length; i++){
//      bytes.add((byte)((num >> (start - i*8)) & 0xFF));
//    }
//    return bytes;
//  }

  public List<Byte> four_int_byte(int length, long num){
    ArrayList<Byte> bytes = new ArrayList<>();
    int start = 8*(length-1);
    for(int i = 0; i < length; i++){
      bytes.add((byte)((num >> (start - i*8)) & 0xFF));
    }
    return bytes;
  }

  public List<Byte> four_int_byte(int length, int num){
    ArrayList<Byte> bytes = new ArrayList<>();
    int start = 8*(length-1);
    for(int i = 0; i < length; i++){
      bytes.add((byte)((num >> (start - i*8)) & 0xFF));
    }
    return bytes;
  }

  public List<Byte> string_byte(String str) {
    List<Byte> str_array = new ArrayList<>();
    for(int i = 0; i < str.length(); i++){
      char ch = str.charAt(i);
      if(ch != '\\')
        str_array.add((byte)(ch & 0xFF));
      else{
        i++;
        ch=str.charAt(i);
        if(ch=='n'){
          ch='\n';
        }else if(ch=='\\'){
          ch='\\';
        }else if(ch=='t'){
          ch='\t';
        }else if(ch=='r'){
          ch='\r';
        }
        str_array.add((byte)(ch & 0xFF));
      }
    }
    return str_array;
  }

  public List<Byte>instruction_byte(Operation opt) {
    if(opt == Operation.nop)
      return four_int_byte(1, 0x00);
    else if(opt == Operation.push)
      return four_int_byte(1,0x01);
    else if(opt == Operation.pop)
      return four_int_byte(1, 0x02);
    else if(opt == Operation.popn)
      return four_int_byte(1, 0x03);
    else if(opt == Operation.dup)
      return four_int_byte(1, 0x04);
    else if(opt == Operation.loca)
      return four_int_byte(1, 0x0a);
    else if(opt == Operation.arga)
      return four_int_byte(1, 0x0b);
    else if(opt == Operation.globa)
      return four_int_byte(1, 0x0c);
    else if(opt == Operation.load_8)
      return four_int_byte(1, 0x10);
    else if(opt == Operation.load_16)
      return four_int_byte(1, 0x11);
    else if(opt == Operation.load_32)
      return four_int_byte(1, 0x12);
    else if(opt == Operation.load_64)
      return four_int_byte(1, 0x13);
    else if(opt == Operation.store_8)
      return four_int_byte(1, 0x14);
    else if(opt == Operation.store_16)
      return four_int_byte(1, 0x15);
    else if(opt == Operation.store_32)
      return four_int_byte(1, 0x16);
    else if(opt == Operation.store_64)
      return four_int_byte(1, 0x17);
    else if(opt == Operation.alloc)
      return four_int_byte(1, 0x18);
    else if(opt == Operation.free)
      return four_int_byte(1, 0x19);
    else if(opt == Operation.stackalloc)
      return four_int_byte(1, 0x1a);
    else if(opt == Operation.add_i)
      return four_int_byte(1, 0x20);
    else if(opt == Operation.sub_i)
      return four_int_byte(1, 0x21);
    else if(opt == Operation.mul_i)
      return four_int_byte(1, 0x22);
    else if(opt == Operation.div_i)
      return four_int_byte(1, 0x23);
    else if(opt == Operation.add_f)
      return four_int_byte(1, 0x24);
    else if(opt == Operation.sub_f)
      return four_int_byte(1, 0x25);
    else if(opt == Operation.mul_f)
      return four_int_byte(1, 0x26);
    else if(opt == Operation.div_f)
      return four_int_byte(1, 0x27);
    else if(opt == Operation.div_u)
      return four_int_byte(1, 0x28);
    else if(opt == Operation.shl)
      return four_int_byte(1, 0x29);
    else if(opt == Operation.shr)
      return four_int_byte(1, 0x2a);
    else if(opt == Operation.and)
      return four_int_byte(1, 0x2b);
    else if(opt == Operation.or)
      return four_int_byte(1, 0x2c);
    else if(opt == Operation.xor)
      return four_int_byte(1, 0x2d);
    else if(opt == Operation.not)
      return four_int_byte(1, 0x2e);
    else if(opt == Operation.cmp_i)
      return four_int_byte(1, 0x30);
    else if(opt == Operation.cmp_u)
      return four_int_byte(1, 0x31);
    else if(opt == Operation.cmp_f)
      return four_int_byte(1, 0x32);
    else if(opt == Operation.neg_i)
      return four_int_byte(1, 0x34);
    else if(opt == Operation.neg_f)
      return four_int_byte(1, 0x35);
    else if(opt == Operation.itof)
      return four_int_byte(1, 0x36);
    else if(opt == Operation.ftoi)
      return four_int_byte(1, 0x37);
    else if(opt == Operation.shrl)
      return four_int_byte(1, 0x38);
    else if(opt == Operation.set_lt)
      return four_int_byte(1, 0x39);
    else if(opt == Operation.set_gt)
      return four_int_byte(1, 0x3a);
    else if(opt == Operation.br)
      return four_int_byte(1, 0x41);
    else if(opt == Operation.br_false)
      return four_int_byte(1, 0x42);
    else if(opt == Operation.br_true)
      return four_int_byte(1, 0x43);
    else if(opt == Operation.call)
      return four_int_byte(1, 0x48);
    else if(opt == Operation.ret)
      return four_int_byte(1, 0x49);
    else if(opt == Operation.callname)
      return four_int_byte(1, 0x4a);
    else if(opt == Operation.scan_i)
      return four_int_byte(1, 0x50);
    else if(opt == Operation.scan_c)
      return four_int_byte(1, 0x51);
    else if(opt == Operation.scan_f)
      return four_int_byte(1, 0x52);
    else if(opt == Operation.print_i)
      return four_int_byte(1, 0x54);
    else if(opt == Operation.print_c)
      return four_int_byte(1, 0x55);
    else if(opt == Operation.print_f)
      return four_int_byte(1, 0x56);
    else if(opt == Operation.print_s)
      return four_int_byte(1, 0x57);
    else if(opt == Operation.println)
      return four_int_byte(1, 0x58);
    else if(opt == Operation.panic)
      return four_int_byte(1, 0xfe);
    return null;
  }
}
