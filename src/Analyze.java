import java.util.*;

public class Analyze {
    public static void main(String[] args) {
        //优先级矩阵映射
        analyser table = new analyser();
        //栈
        Stack stack = new Stack();
        //字符串
        Stack msg = new Stack();
        //栈初始化
        stack.push('#');
        msg.push('#');
        //
        int flag = 0;

        Scanner reader = new Scanner(System.in);
        String s = reader.nextLine();
        for(int i = s.length()-1; i >= 0; i--){
            msg.push(s.charAt(i));
        }
        //从字符串取一个字符
        char now = (char) msg.peek();
        while((!msg.peek().equals('#')) || (!stack.peek().equals('N')) || (!stack.get(0).equals('#')) || (stack.size()!=2)){
            if(!(now=='*') && !(now=='+') && !(now=='(') && !(now==')') && !(now=='i') && !(now=='#')){
                System.out.println('E');
                break;
            }
            //栈
            char snow,l_tmp,r_tmp;
            l_tmp = r_tmp = '\0';
            //如果栈顶是非终结符
            if(stack.peek().equals('N')){
                r_tmp = (char)stack.pop();
                snow = (char)stack.peek();
                flag = 1;
            }
            else{
                snow = (char)stack.peek();
            }
            //匹配优先级
            int priority = table.priority[table.map.get(snow)][table.map.get(now)];
            //写入
            if(priority >= 1){
                if(flag == 1){
                    flag = 0;
                    stack.push(r_tmp);
                }
                msg.pop();
                System.out.println("I" + now);
                stack.push(now);
                now = (char)msg.peek();
                continue;
            }
            //出错
            else if(priority == -1){
                System.out.println('E');
                break;
            }
            //规约
            else{
                if(snow == 'i'){
                    stack.pop();
                    stack.push('N');
                    System.out.println('R');
                    continue;
                }
                else if(snow == '+'){
                    stack.pop();
                    l_tmp = (char) stack.peek();
                    if((l_tmp == 'N') && (r_tmp == 'N')){
                        stack.pop();
                        stack.push('N');
                        System.out.println('R');
                        continue;
                    }
                    else{
                        System.out.println("RE");
                        break;
                    }
                }
                else if(snow == '*'){
                    stack.pop();
                    l_tmp = (char) stack.peek();
                    if((l_tmp == 'N') && (r_tmp == 'N')){
                        stack.pop();
                        stack.push('N');
                        System.out.println('R');
                        continue;
                    }
                    else{
                        System.out.println("RE");
                        break;
                    }
                }
                else if(snow == '('){
                    System.out.println("RE");
                    break;
                }
                else if(snow == ')'){
                    stack.pop();
                    l_tmp = (char) stack.pop();
                    char lp = (char) stack.pop();
                    if((lp=='(') && (l_tmp=='N')){
                        stack.push('N');
                        System.out.println('R');
                        continue;
                    }
                    else{
                        System.out.println("RE");
                        break;
                    }
                }
                else{
                    System.out.println("RE");
                    break;
                }
            }
        }
    }
}
class analyser {
    //>0 <1 =2
    int[][] priority = {
            {0, 1, 1, 1, 0, 0},
            {0, 0, 1, 1, 0, 0},
            {0, 0, -1, -1, 0, 0},
            {1, 1, 1, 1, 2, 0},
            {0, 0, -1, -1, 0, 0},
            {1, 1, 1, 1, 1, 2}
    };
    Map<Character, Integer> map;

    analyser() {
        map = new HashMap<Character, Integer>();
        map.put('+', 0);
        map.put('*', 1);
        map.put('i', 2);
        map.put('(', 3);
        map.put(')', 4);
        map.put('#', 5);
    }
}
