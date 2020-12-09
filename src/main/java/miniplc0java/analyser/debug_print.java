package miniplc0java.analyser;

public class debug_print {
    public static void print_stack(int stack_top1, int stack_top2, StackVar[] stack_vars, StackPoint[] stack_points){
        String[] type={"Unknown","int","void","double"};
        while(stack_top2>=0) {
            for (int i = stack_top1; i >= stack_points[stack_top2].Value; i--) {
                System.out.println(stack_vars[i].name + '\t' + type[stack_vars[i].type]);
            }
            if(stack_points[stack_top2].isFn){
                System.out.print("fn");
            }
            System.out.println("------------------"+stack_top2);
            stack_top1=stack_points[stack_top2].Value-1;
            stack_top2--;
        }
    }
    public static void print_current_stack(int stack_top1, int stack_top2, StackVar[] stack_vars, StackPoint[] stack_points){
        String[] type={"Unknown","int","void","double"};
        for (int i = stack_top1; i >= stack_points[stack_top2].Value; i--) {
            System.out.println(stack_vars[i].name + '\t' + type[stack_vars[i].type]);
        }
        if(stack_points[stack_top2].isFn){
            System.out.print("fn");
        }else{
            System.out.print("++");
        }
        System.out.println("++++++++++++++++"+stack_top2);
    }
//    public static void print_funcs(func[] func_list,int func_top){
//        func tmp;
//        for(int i=func_top;i>=0;i--){
//            tmp=func_list[i];
//            String str=new StringBuilder().append("fn [").append(tmp.global_num).append("] ")
//                    .append(tmp.locals_num).append(" ").append(tmp.args_num).append(" -> ")
//                    .append(tmp.return_num).toString();
//            System.out.println(str);
//        }
//    }
}