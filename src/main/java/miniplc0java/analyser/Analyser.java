package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.ExpectedTokenError;
import miniplc0java.error.TokenizeError;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;
    public StackVar[] stackLeft = new StackVar[1000];
    public StackPoint[] stackRight = new StackPoint[1000];
    public int leftP = -1;
    public int rightP = -1;
    StackVar varTop = null;
    boolean stackFnFlag = false;
    public Function currentFunc = null;
    public Function[] funcList = new Function[1000];
    public int funcP = -1;
    int cntParam = 0;
    int cntLocal = 0;
    Map<String, Function> funcMap = new HashMap<String, Function>();
    int checkExprType = 0;
    Boolean isSingleFunc = false;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();

    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        exprToken[++callFuncP] = new ArrayList<>();
        initialStack();
        analyseProgram();
        //加载main
        Function mainFun = funcMap.get("main");
        int mainReturn = mainFun.returnType;
        //如果main的返回值是void
        if(mainReturn == 2){
            currentFunc.addOperations(new Instruction(Operation.stackalloc,0));
        }
        else{
            currentFunc.addOperations(new Instruction(Operation.stackalloc,1));
        }
        currentFunc.addOperations(new Instruction(Operation.call,mainFun.funcNum));
        //pop main
        if(mainReturn != 2){
            currentFunc.addOperations(new Instruction(Operation.popn,1));
        }
//        debug_print.print_stack(leftP, rightP, stackLeft, stackRight);
        return instructions;
    }

    /**
     * 查看下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            do{
                peekedToken = tokenizer.nextToken();
            }while(peekedToken.getTokenType().equals(TokenType.COMMENT));
            System.out.println(peekedToken);
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            Token tmp;
            do{
                tmp = tokenizer.nextToken();
            }while(tmp.getTokenType().equals(TokenType.COMMENT));
            return tmp;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     * 
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     * 
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     * 
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移
     * 
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }


    /**
     * 压栈
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, boolean isFunction, Pos curPos, int type, TypeAndPos typeAndPos) throws AnalyzeError {
        if (stackCurrent(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            stackLeft[++leftP] = new StackVar(isConstant, isInitialized, isFunction, name, type, typeAndPos);
        }
    }

    /**
     * 从局部->全局检查引用情况
     */
//    private stackVar checkSymbol(String name, Pos curPos) throws AnalyzeError {
//        while(rightP >= 0) {
//            for (int i = stackRight[rightP].Value; i <= leftP; i++) {
//                if (stackLeft[i].name.equals(name)) {
//                    return stackLeft[i];
//                }
//            }
//            rightP--;
//        }
//        throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
//    }

    private StackVar stackCurrent(String name) {
//        System.out.println(rightP);
        for(int i = stackRight[rightP].Value; i <= leftP; i++){
            if(stackLeft[i].name.equals(name)){
                return stackLeft[i];
            }
        }
        return null;
    }

    private StackVar stackAll(String name) {
        int currentP = leftP;
        for(int i = rightP; i >= 0; i--){
             for(int j = currentP; j >= stackRight[i].Value; j--){
                 if(stackLeft[j].name.equals(name)){
                     return stackLeft[j];
                 }
             }
             currentP = stackRight[i].Value - 1;
        }
        return null;
    }

    /**
     * 设置符号为已赋值
     * 
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void declareSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     * 
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getStackOffset();
        }
    }

    /**
     * 获取变量是否是常量
     * 
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    /**
     * 初始化栈
     */
    public void initialStack(){
        stackRight[++rightP] = new StackPoint(true,0);
        stackLeft[++leftP] = new StackVar(true,true,true,"getint",1, new TypeAndPos(1,0));
        stackLeft[++leftP] = new StackVar(true,true,true,"getdouble",3, new TypeAndPos(1, 1));
        stackLeft[++leftP] = new StackVar(true,true,true,"getchar",1, new TypeAndPos(1,2));
        stackLeft[++leftP] = new StackVar(true,true,true,"putint",2, new TypeAndPos(1,3));
        stackLeft[++leftP] = new StackVar(true,true,true,"putdouble",2, new TypeAndPos(1,4));
        stackLeft[++leftP] = new StackVar(true,true,true,"putchar",2, new TypeAndPos(1,5));
        stackLeft[++leftP] = new StackVar(true,true,true,"putstr",2, new TypeAndPos(1,6));
        stackLeft[++leftP] = new StackVar(true,true,true,"putln",2, new TypeAndPos(1,7));
        stackLeft[++leftP] = new StackVar(true,true,true,"_start",2, new TypeAndPos(1,8));
        funcList[++funcP] = new Function(0,8,0,2,0, false);
        funcMap.put("getint",new Function(0,0,0,1,-1,true));
        funcMap.put("getdouble",new Function(0,1,0,3,-1,true));
        funcMap.put("getchar",new Function(0,2,0,1,-1,true));
        funcMap.put("putint",new Function(1,3,0,2,-1,true));
        funcMap.put("putdouble",new Function(1,4,0,2,-1,true));
        funcMap.put("putchar",new Function(1,5,0,2,-1,true));
        funcMap.put("putstr",new Function(1,6,0,2,-1,true));
        funcMap.put("putln",new Function(0,7,0,2,-1,true));
        currentFunc = funcList[funcP];
    }

    /**
     * item -> function | decl_stmt
     * program -> item*
     */
    private void analyseProgram() throws CompileError {
        while(check(TokenType.FN_KW) || check(TokenType.LET_KW) || check(TokenType.CONST_KW)){
            if(check(TokenType.FN_KW)){
                analyseFunction();
            }
            else{
                analyseDeclStmt();
            }
        }
        expect(TokenType.EOF);
//        System.out.println("okkkkkkkkkkk!!!");
    }

    /**
     * decl_stmt -> let_decl_stmt | const_decl_stmt
     */
    private void analyseDeclStmt() throws CompileError{
        if(check(TokenType.LET_KW)){
            analyseLetDeclStmt();
        }
        else{
            analyseConstDeclStmt();
        }
    }

    /**
     * const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     */
    private void analyseConstDeclStmt() throws CompileError{
        expect(TokenType.CONST_KW);
        Token token = expect(TokenType.IDENT);
        TypeAndPos typeAndPos = null;
        if(rightP == 0){
            typeAndPos = new TypeAndPos(1,leftP);
        }
        else{
            typeAndPos = new TypeAndPos(2,cntLocal);
            cntLocal++;
        }
        getAddress(typeAndPos);
        addSymbol(token.getValueString(),true,true, false, token.getStartPos(),1, typeAndPos);
        expect(TokenType.COLON);
        int varType = analyseTyParam();
        checkExprType = varType;
        expect(TokenType.ASSIGN);
        exprToken[callFuncP].clear();
        analyseExpr();
//        debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
        transExpr(MidToLast.midToLast(exprToken[callFuncP]));
        currentFunc.addOperations(new Instruction(Operation.store_64));
        expect(TokenType.SEMICOLON);
        varTop = stackLeft[leftP];
        varTop.type = varType;
    }

    //-------------------------------------表达式-----------------------------------
    CallFunc[] callFuncs = new CallFunc[1000];
    ArrayList[] exprToken = new ArrayList[1000];
    int callFuncP = -1;
    /**
     * expr ->
     *       operator_expr (expr)
     *     | negate_expr
     *     | assign_expr
     *     | as_expr (expr)
     *     | call_expr
     *     | literal_expr
     *     | ident_expr
     *     | group_expr
     */
    private void analyseExpr() throws CompileError{
        Boolean expr = false;
        //negate_expr
        if(analyseNegateExpr()){
            expr = true;
        }
        if(!expr){
            //assign_expr or call_expr or ident_expr
            if(analyseAssignOrCallOrIdentExpr()){
                expr = true;
            }
            //literal_expr
            else if(analyseLiteralExpr()){
                expr = true;
            }
            //group_expr
            else if(analyseGroupExpr()){
                expr = true;
            }
        }
        if(!expr){
            throw  new AnalyzeError(ErrorCode.IncompleteExpression,peek().getStartPos());
        }
       if(expr){
            //as_expr -> expr 'as' ty
            if(check(TokenType.AS_KW)){
                expect(TokenType.AS_KW);
                analyseTyParam();
            }
            //operator_expr -> expr binary_operator expr
           else{
               if(analyseBinaryOperator()) {
                   
                   analyseExpr();
                   
               }
            }
        }
    }

    /**
     * binary_operator -> '+' | '-' | '*' | '/' | '==' | '!=' | '<' | '>' | '<=' | '>='
     */
    private boolean analyseBinaryOperator() throws CompileError{
        // *
        if(check(TokenType.MUL)){
            expect(TokenType.MUL);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.MUL));
            return true;
        }
        // -
        else if(check(TokenType.MINUS)){
            expect(TokenType.MINUS);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.MINUS));
            return true;
        }
        // +
        else if(check(TokenType.PLUS)){
            expect(TokenType.PLUS);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.PLUS));
            return true;
        }
        // /
        else if(check(TokenType.DIV)){
            expect(TokenType.DIV);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.DIV));
            return true;
        }
        // ==
        else if(check(TokenType.EQ)){
            expect(TokenType.EQ);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.EQ));
            return true;
        }
        // !=
        else if(check(TokenType.NEQ)){
            expect(TokenType.NEQ);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.NEQ));
            return true;
        }
        // <
        else if(check(TokenType.LT)){
            expect(TokenType.LT);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.LT));
            return true;
        }
        // >
        else if(check(TokenType.GT)){
            expect(TokenType.GT);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.GT));
            return true;
        }
        // <=
        else if(check(TokenType.LE)){
            expect(TokenType.LE);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.LE));
            return true;
        }
        // >=
        else if(check(TokenType.GE)){
            expect(TokenType.GE);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.GE));
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * group_expr -> '(' expr ')'
     */
    private boolean analyseGroupExpr() throws CompileError{
        if(check(TokenType.L_PAREN)) {
            expect(TokenType.L_PAREN);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.L_PAREN));
            analyseExpr();
            expect(TokenType.R_PAREN);
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.R_PAREN));
            return true;
        }
        return false;
    }

    Boolean assignFlag = false;
    /**
     * assign_expr -> l_expr '=' expr
     * l_expr -> IDENT
     *
     * call_expr -> IDENT '(' call_param_list? ')'
     * ident_expr -> IDENT
     */
    Boolean checkIsFirstAssign = true;
    private boolean analyseAssignOrCallOrIdentExpr() throws CompileError{
        if(check(TokenType.IDENT)){
            //全局寻找引用
            Token token = expect(TokenType.IDENT);
            StackVar tmp = stackAll(token.getValueString());

            //引用没有放在符号表中
            if (tmp == null) {
                throw new AnalyzeError(ErrorCode.NotDeclared, token.getStartPos());
            }
            //call_expr -> IDENT '(' call_param_list? ')'
            if(check(TokenType.L_PAREN)){
                //栈增
                Function function = funcMap.get(token.getValueString());
                if(checkExprType!=-1&&checkExprType != function.returnType){
                    throw new AnalyzeError(ErrorCode.InvalidReturnType,token.getStartPos());
                }
                CallFunc tmpFunc=new CallFunc(ExpressionType.FUNC, function);
                exprToken[callFuncP].add(tmpFunc);
                callFuncs[++callFuncP] = tmpFunc;
                exprToken[callFuncP] = new ArrayList<ExpressionToken>();

                expect(TokenType.L_PAREN);
                if(!check(TokenType.R_PAREN)) {
                    analyseCallParamList();
                }
                expect(TokenType.R_PAREN);
                //栈减
                callFuncP--;

                return true;
            }
            //assign_expr -> l_expr '=' expr
            else if(check(TokenType.ASSIGN)){
                assignFlag = true;
                isSingleFunc = false;
                checkExprType = tmp.type;
                //常量不能被赋值
                if(tmp.isConst){
                    throw new AnalyzeError(ErrorCode.AssignToConstant, token.getStartPos());
                }
                tmp.isInitialized = true;
                getAddress(tmp.typeAndPos);
                expect(TokenType.ASSIGN);
                if(checkIsFirstAssign == false){
                    throw new AnalyzeError(ErrorCode.InvalidAssignment, token.getStartPos());
                }
                checkIsFirstAssign = false;
                exprToken[callFuncP].clear();
                analyseExpr();
                checkIsFirstAssign = true;
                return true;
            }
            //ident_expr -> IDENT
            else{
                exprToken[callFuncP].add(new Var(ExpressionType.Var,tmp));
                //等号右边的INDENT必须已经初始化
                if(!tmp.isInitialized){
                    throw new AnalyzeError(ErrorCode.NotInitialized, token.getStartPos());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * literal_expr -> UINT_LITERAL | DOUBLE_LITERAL | STRING_LITERAL | CHAR_LITERAL
     */
    private boolean analyseLiteralExpr() throws CompileError{
        //UINT_LITERAL
        if(check(TokenType.UINT_LITERAL)){
            Token token = expect(TokenType.UINT_LITERAL);
            exprToken[callFuncP].add(new Uinteger(ExpressionType.UINT_LITERAL, (long)token.getValue()));
            return true;
        }
        //STRING_LITERAL
        else if(check(TokenType.STRING_LITERAL)){
            expect(TokenType.STRING_LITERAL);
            return true;
        }
        return false;
    }


    /**
     * call_param_list -> expr (',' expr)*
     */
    private void analyseCallParamList() throws CompileError{
        analyseExpr();
        callFuncs[callFuncP].addParam(MidToLast.midToLast(exprToken[callFuncP]));
        while(check(TokenType.COMMA)){
            expect(TokenType.COMMA);
            exprToken[callFuncP].clear();
            analyseExpr();
            callFuncs[callFuncP].addParam(MidToLast.midToLast(exprToken[callFuncP]));
        }
    }

    /**
     * negate_expr -> '-' expr
     */
    private boolean analyseNegateExpr() throws CompileError{
        if(check(TokenType.MINUS)){
            exprToken[callFuncP].add(new ExpressionToken(ExpressionType.NEG));
            expect(TokenType.MINUS);
            analyseExpr();
            return true;
        }
        return false;
    }


    //---------------------------------------语句---------------------------------------------
    /**
     * let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     */
    private void analyseLetDeclStmt() throws CompileError{
        Boolean isInitialized = false;
        expect(TokenType.LET_KW);
        Token token = expect(TokenType.IDENT);
        TypeAndPos typeAndPos;
        if(rightP == 0){
            typeAndPos = new TypeAndPos(1,leftP+1);
        }
        else{
            typeAndPos = new TypeAndPos(2,cntLocal);
            cntLocal++;
        }
        addSymbol(token.getValueString(),false,false,false,token.getStartPos(),1, typeAndPos);
        expect(TokenType.COLON);
        int varType = analyseTyParam();
        checkExprType = varType;
        if(!check(TokenType.SEMICOLON)) {
            getAddress(typeAndPos);
            expect(TokenType.ASSIGN);
            exprToken[callFuncP].clear();
            analyseExpr();
//            debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
            transExpr(MidToLast.midToLast(exprToken[callFuncP]));
            currentFunc.addOperations(new Instruction(Operation.store_64));
            isInitialized = true;
        }
        expect(TokenType.SEMICOLON);
        varTop = stackLeft[leftP];
        varTop.type = varType;
        varTop.isInitialized = isInitialized;
    }

    /**
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     */
    private void analyseFunction() throws CompileError{
        expect(TokenType.FN_KW);
        //把函数名入栈
        Token token = expect(TokenType.IDENT);
        TypeAndPos typeAndPos = new TypeAndPos(1,leftP+1);
        //如果找到了fn
        if(funcMap.get(token.getValueString()) != null){
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, token.getStartPos());
        }
        //如果没有找到fn
        else{
            addSymbol(token.getValueString(),false,true,true,token.getStartPos(),0,typeAndPos);
        }
        //把新的func入栈
        cntParam = 0;
        cntLocal = 0;
        funcList[++funcP] = new Function(0,leftP,0,0,funcP, false);
        funcMap.put(token.getValueString(),funcList[funcP]);
        currentFunc = funcList[funcP];
        expect(TokenType.L_PAREN);
        //新建一个局部栈
        newStack();
        //如果是函数局部栈, 到大括号时跳过
        stackFnFlag = true;
        if(!check(TokenType.R_PAREN)) {
            analyseFunctionParamList();
        }
        expect(TokenType.R_PAREN);
        currentFunc.paramNum = cntParam;
        expect(TokenType.ARROW);
        int varType = analyseTyFn();
        currentFunc.returnType = varType;

        analyseBlockStmt();
        currentFunc.localNum = cntLocal;
        varTop = stackLeft[leftP];
        varTop.type = varType;
        //如果返回值为void的函数没有return
        if(currentFunc.operations.get(currentFunc.operations.size()-1).opt!=Operation.ret){
            currentFunc.addOperations(new Instruction(Operation.ret));
        }
        //退回start
        currentFunc = funcList[0];
    }

    /**
     * 新的局部栈*/
    private void newStack() {
        stackRight[++rightP] = new StackPoint(true, leftP+1);
    }

    /**
     * block_stmt -> '{' stmt* '}'
     */
    private void analyseBlockStmt() throws CompileError{
        expect(TokenType.L_BRACE);
        //如果不是函数局部栈
        if(!stackFnFlag){
//            System.out.println(111111);
           newStack();
           stackRight[rightP].isFn = false;
        }
        //如果是函数局部栈则跳过
        else{
            stackFnFlag = false;
        }
        while(!check(TokenType.R_BRACE)){
            analyseStmt();
        }
        expect(TokenType.R_BRACE);
        //弹出局部栈
//        debug_print.print_current_stack(leftP, rightP, stackLeft, stackRight);
        leftP = stackRight[rightP].Value - 1;
        rightP--;
    }

    /**
     * stmt ->
     *       expr_stmt (last)
     *     | decl_stmt
     *     | if_stmt
     *     | while_stmt
     *     | break_stmt
     *     | continue_stmt
     *     | return_stmt
     *     | block_stmt
     *     | empty_stmt*/
    private void analyseStmt() throws CompileError{
        //decl_stmt
        if(check(TokenType.CONST_KW) || check(TokenType.LET_KW)){
            analyseDeclStmt();
        }
        //if_stmt
        else if(check(TokenType.IF_KW)){
            analyseIFStmt();
        }
        //while_stmt
        else if(check(TokenType.WHILE_KW)){
            analyseWhileStmt();
        }
        //break_stmt
        //continue_stmt
        //return_stmt
        else if(check(TokenType.RETURN_KW)){
            analyseReturnStmt();
        }
        //block_stmt
        else if(check(TokenType.L_BRACE)){
            analyseBlockStmt();
        }
        //empty_stmt
        else if(check(TokenType.SEMICOLON)){
            analyseEmptyStmt();
        }
        //expr_stmt
        else{
            isSingleFunc = true;
            analyseExprStmt();
            isSingleFunc = false;
        }
    }

    /**
     * expr_stmt -> expr ';'
     */
    private void analyseExprStmt() throws CompileError{
        exprToken[callFuncP].clear();
        checkExprType=-1;
        analyseExpr();
        transExpr(MidToLast.midToLast(exprToken[callFuncP]));
        if(assignFlag){
            currentFunc.addOperations(new Instruction(Operation.store_64));
            assignFlag = false;
        }
//        debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
        expect(TokenType.SEMICOLON);
    }

    /**
     * empty_stmt -> ';'
     */
    private void analyseEmptyStmt() throws CompileError{
        expect(TokenType.SEMICOLON);
    }

    /**
     * return_stmt -> 'return' expr? ';'
     */
    private void analyseReturnStmt() throws CompileError{
        Token token = expect(TokenType.RETURN_KW);
        //如果有表达式
        if(!check(TokenType.SEMICOLON)) {
            //加载返回值的地址
            currentFunc.addOperations(new Instruction(Operation.arga,0));
            exprToken[callFuncP].clear();
            analyseExpr();
            transExpr(MidToLast.midToLast(exprToken[callFuncP]));
            currentFunc.addOperations(new Instruction(Operation.store_64));
//            debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
            if(currentFunc.returnType != 1){
                System.out.println(stackLeft[currentFunc.globalNum].name);
                throw new AnalyzeError(ErrorCode.InvalidReturnType, token.getStartPos());
            }
        }
        //如果没有表达式
        else{
            if(currentFunc.returnType != 2){
                throw new AnalyzeError(ErrorCode.InvalidReturnType, token.getStartPos());
            }
        }
        currentFunc.addOperations(new Instruction(Operation.ret));
        expect(TokenType.SEMICOLON);
    }

    /**
     * while_stmt -> 'while' expr block_stmt
     */
    private void analyseWhileStmt() throws CompileError{
        expect(TokenType.WHILE_KW);
        exprToken[callFuncP].clear();
        int cnt = currentFunc.operations.size();
        currentFunc.addOperations(new Instruction(Operation.br,0));
        analyseExpr();
        transExpr(MidToLast.midToLast(exprToken[callFuncP]));
        currentFunc.addOperations(new Instruction(Operation.br_true,1));
        Instruction whileBr = new Instruction(Operation.br,currentFunc.operations.size());
        currentFunc.addOperations(whileBr);
//        debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
        analyseBlockStmt();
        whileBr.setArg1(currentFunc.operations.size()-whileBr.arg1);
        currentFunc.addOperations(new Instruction(Operation.br,cnt-currentFunc.operations.size()));
    }

    ArrayList<Instruction> brs = new ArrayList<Instruction>();
    /**
     * if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
     */
    private void analyseIFStmt() throws CompileError {
        expect(TokenType.IF_KW);
        exprToken[callFuncP].clear();
        analyseExpr();
//        debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
        transExpr(MidToLast.midToLast(exprToken[callFuncP]));
        //br
        Instruction ifBr = new Instruction(Operation.br,0);
        currentFunc.addOperations(new Instruction(Operation.br_true,1));
        currentFunc.addOperations(ifBr);
        int cntBr = currentFunc.operations.size();
        //br over
        analyseBlockStmt();
        //设置后面需要跳过的br
        Instruction tmpBr = new Instruction(Operation.br, currentFunc.operations.size());
        brs.add(tmpBr);
        currentFunc.addOperations(tmpBr);
        ifBr.setArg1(currentFunc.operations.size()-cntBr);
        //跳过br over
        while (check(TokenType.ELSE_KW)){
            expect(TokenType.ELSE_KW);
            if(check(TokenType.IF_KW)) {
                expect(TokenType.IF_KW);
                exprToken[callFuncP].clear();
                analyseExpr();
//                debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
                transExpr(MidToLast.midToLast(exprToken[callFuncP]));
                //br
                Instruction elseIfBr = new Instruction(Operation.br);
                currentFunc.addOperations(new Instruction(Operation.br_true,1));
                currentFunc.addOperations(elseIfBr);
                int cntBr_2 = currentFunc.operations.size();
                //br over
                analyseBlockStmt();
                Instruction tmpBr_2 = new Instruction(Operation.br, currentFunc.operations.size());
                brs.add(tmpBr_2);
                currentFunc.addOperations(tmpBr_2);
                elseIfBr.setArg1(currentFunc.operations.size()-cntBr_2);
            }
            else{
                analyseBlockStmt();
                break;
            }
        }
        for(int i = 0; i < brs.size(); i++){
            brs.get(i).setArg1(currentFunc.operations.size()-brs.get(i).arg1-1);
        }
        brs.clear();
    }

    private void getAddress(TypeAndPos typeAndPos) {
        //global
        if(typeAndPos.type == 1){
            currentFunc.addOperations(new Instruction(Operation.globa, typeAndPos.position));
        }
        //local
        else if(typeAndPos.type == 2){
            currentFunc.addOperations(new Instruction(Operation.loca, typeAndPos.position));
        }
        //param
        else if(typeAndPos.type == 3){
            //如果返回值是void, 参数-1
            if(currentFunc.returnType == 2){
                currentFunc.addOperations(new Instruction(Operation.arga, typeAndPos.position-1));
            }
            else{
                currentFunc.addOperations(new Instruction(Operation.arga, typeAndPos.position));
            }
        }
    }

    //表达式转指令
    private void transExpr(ArrayList<ExpressionToken> arrayList) {
        for(int i = 0; i < arrayList.size(); i++){
            ExpressionToken exprToken = arrayList.get(i);
            //变量
            if(exprToken.type == ExpressionType.Var){
                Var var = (Var)exprToken;
                getAddress(var.varValue.typeAndPos);
                currentFunc.addOperations(new Instruction(Operation.load_64));
            }
            //常量
            else if(exprToken.type == ExpressionType.UINT_LITERAL){
                currentFunc.addOperations(new Instruction(Operation.push,((Uinteger) exprToken).value));
            }
            //函数
            else if(exprToken.type == ExpressionType.FUNC){
                CallFunc callfunc = (CallFunc) exprToken;
                Function tmpFunc = callfunc.function;
                System.out.println(tmpFunc);
                //如果返回值是void,需要预留空间
                if(tmpFunc.returnType == 2){
                    currentFunc.addOperations(new Instruction(Operation.stackalloc, 0));
                }
                else{
                    currentFunc.addOperations(new Instruction(Operation.stackalloc, 1));
                }
                //从第2层嵌套开始就一定不是单条函数
                Boolean tmp = isSingleFunc;
                isSingleFunc = false;
                //参数->指令
                for(int j = 0; j < callfunc.param.size(); j++){
                    transExpr(callfunc.param.get(j));
                }
                //如果是内置函数
                if(tmpFunc.isInner){
                    currentFunc.addOperations(new Instruction(Operation.callname,tmpFunc.globalNum));
                }
                else{
                    currentFunc.addOperations(new Instruction(Operation.call,tmpFunc.funcNum));
                }
                //复原
                isSingleFunc = tmp;
                //如果是单条语句且返回值不是void需要手动pop
                if(tmp && tmpFunc.returnType != 2){
                    currentFunc.addOperations(new Instruction(Operation.popn,1));
                }
            }
            //其他
            else{
                // 负-
                if(exprToken.type == ExpressionType.NEG){
                    currentFunc.addOperations(new Instruction(Operation.neg_i));
                }
                // +
                else if(exprToken.type == ExpressionType.PLUS){
                    currentFunc.addOperations(new Instruction(Operation.add_i));
                }
                // 减-
                else if(exprToken.type == ExpressionType.MINUS){
                    currentFunc.addOperations(new Instruction(Operation.sub_i));
                }
                // *
                else if(exprToken.type == ExpressionType.MUL){
                    currentFunc.addOperations(new Instruction(Operation.mul_i));
                }
                // /
                else if(exprToken.type == ExpressionType.DIV){
                    currentFunc.addOperations(new Instruction(Operation.div_i));
                }
                // <
                else if(exprToken.type == ExpressionType.LT){
                    currentFunc.addOperations(new Instruction(Operation.cmp_i));
                    currentFunc.addOperations(new Instruction(Operation.set_lt));
                }
                // >
                else if(exprToken.type == ExpressionType.GT){
                    currentFunc.addOperations(new Instruction(Operation.cmp_i));
                    currentFunc.addOperations(new Instruction(Operation.set_gt));
                }
                // <=
                else if(exprToken.type == ExpressionType.LE){
                    currentFunc.addOperations(new Instruction(Operation.cmp_i));
                    currentFunc.addOperations(new Instruction(Operation.set_gt));
                    currentFunc.addOperations(new Instruction(Operation.not));
                }
                // >=
                else if(exprToken.type == ExpressionType.GE){
                    currentFunc.addOperations(new Instruction(Operation.cmp_i));
                    currentFunc.addOperations(new Instruction(Operation.set_lt));
                    currentFunc.addOperations(new Instruction(Operation.not));
                }
                // =
                else if(exprToken.type == ExpressionType.EQ){
                    currentFunc.addOperations(new Instruction(Operation.cmp_i));
                    currentFunc.addOperations(new Instruction(Operation.not));
                }
                // !=
                else if(exprToken.type == ExpressionType.NEQ){
                    currentFunc.addOperations(new Instruction(Operation.cmp_i));
                }
                // panic
                else{
                    currentFunc.addOperations(new Instruction(Operation.panic));
                }
            }
        }
    }

    /**
     * 函数返回类型: int / void
     * ty -> IDENT
     */
    private int analyseTyFn() throws CompileError{
        if(check(TokenType.VOID)){
            expect(TokenType.VOID);
            return 2;
        }
        else{
            expect(TokenType.INTEGER);
            return 1;
        }
    }
    /**
     * 参数类型: int
     */
    private int analyseTyParam() throws CompileError{
            expect(TokenType.INTEGER);
            return 1;
    }

    /**
     * function_param_list -> function_param (',' function_param)*
     */
    private void analyseFunctionParamList() throws CompileError{
        cntParam++;
        analyseFunctionParam();
        while(check(TokenType.COMMA)){
            cntParam++;
            expect(TokenType.COMMA);
            analyseFunctionParam();
        }
    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     */
    private void analyseFunctionParam() throws CompileError{
        TypeAndPos typeAndPos = null;
        if(!check(TokenType.IDENT)) {
            expect(TokenType.CONST_KW);
            Token token = expect(TokenType.IDENT);
            typeAndPos = new TypeAndPos(3,cntParam);
            addSymbol(token.getValueString(),true,true, false,token.getStartPos(),0, typeAndPos);
        }
        else{
            Token token = expect(TokenType.IDENT);
            typeAndPos = new TypeAndPos(3,cntParam);
            addSymbol(token.getValueString(),true,false,false,token.getStartPos(),0, typeAndPos);
        }
        varTop = stackLeft[leftP];
        expect(TokenType.COLON);
        varTop.type = analyseTyParam();
    }
}

