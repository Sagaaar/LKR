package miniplc0java.instruction;

public enum Operation {
    //指令   指令名   操作数       弹栈     压栈     介绍
    nop,    //0x00  nop	    -	        -	    -	    空指令
    push,   //0x01  push	num:u64	    -	    1:num	将 num 压栈
    pop,    //0x02  pop	    -	        1	    -       弹栈 1 个 slot
    popn,   //0x03	popn	num:u32	    1-num	-	    弹栈 num 个 slot
    dup,    //0x04	dup 	-	        1:num	1:num, 2:num	复制栈顶 slot
    loca,   //0x0a	loca	off:u32	    -	    1:addr	加载 off 个 slot 处局部变量的地址
    arga,
    globa,
    load_8,
    load_16,
    load_32,
    load_64,
    store_8,
    store_16,
    store_32,
    store_64,
    alloc,
    free,
    stackalloc,
    add_i,
    sub_i,
    mul_i,
    div_i,
    add_f,
    sub_f,
    mul_f,
    div_f,
    div_u,
    shl,
    shr,
    and,
    or,
    xor,
    not,
    cmp_i,
    cmp_u,
    cmp_f,
    neg_i,
    neg_f,
    itof,
    ftoi,
    shrl,
    set_lt,
    set_gt,
    br,
    br_false,
    br_true,
    call,
    ret,
    callname,
    scan_i,
    scan_c,
    scan_f,
    print_i,
    print_c,
    print_f,
    print_s,
    println,
    panic
}
