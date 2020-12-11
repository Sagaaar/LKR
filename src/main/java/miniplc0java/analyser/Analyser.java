package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.ExpectedTokenError;
import miniplc0java.error.TokenizeError;
import miniplc0java.instruction.Instruction;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;
    StackVar[] stackLeft = new StackVar[1000];
    StackPoint[] stackRight = new StackPoint[1000];
    public int leftP = -1;
    public int rightP = -1;
    StackVar varTop = null;
    boolean stackFnFlag = false;
    Function currentFunc = null;
    Function[] funcList = new Function[1000];
    int funcP = -1;
    int cntParam = 0;
    int cntLocal = 0;
    Map<String, Function> funcMap = new HashMap<String, Function>();

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
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, Pos curPos, int type, TypeAndPos typeAndPos) throws AnalyzeError {
        if (stackCurrent(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            stackLeft[++leftP] = new StackVar(isConstant, isInitialized, name, type, typeAndPos);
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
        stackLeft[++leftP] = new StackVar(true,true,"getint",1, new TypeAndPos(1,0));
        stackLeft[++leftP] = new StackVar(true,true,"getdouble",3, new TypeAndPos(1, 1));
        stackLeft[++leftP] = new StackVar(true,true,"getchar",1, new TypeAndPos(1,2));
        stackLeft[++leftP] = new StackVar(true,true,"putint",2, new TypeAndPos(1,3));
        stackLeft[++leftP] = new StackVar(true,true,"putdouble",2, new TypeAndPos(1,4));
        stackLeft[++leftP] = new StackVar(true,true,"putchar",2, new TypeAndPos(1,5));
        stackLeft[++leftP] = new StackVar(true,true,"putstr",2, new TypeAndPos(1,6));
        stackLeft[++leftP] = new StackVar(true,true,"putln",2, new TypeAndPos(1,7));
        stackLeft[++leftP] = new StackVar(true,true,"_start",2, new TypeAndPos(1,8));
        funcList[++funcP] = new Function(0,8,0,2,0, false);
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
        }
        addSymbol(token.getValueString(),true,true,token.getStartPos(),1, typeAndPos);
        expect(TokenType.COLON);
        int varType = analyseTyParam();
        expect(TokenType.ASSIGN);
        exprToken[callFuncP].clear();
        analyseExpr();
        debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
        
        expect(TokenType.SEMICOLON);
        varTop = stackLeft[leftP];
        varTop.type = varType;
    }

    //-------------------------------------表达式-----------------------------------
    callFunc[] callFuncs = new callFunc[1000];
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

    /**
     * assign_expr -> l_expr '=' expr
     * l_expr -> IDENT
     *
     * call_expr -> IDENT '(' call_param_list? ')'
     * ident_expr -> IDENT
     */
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
                callFunc tmpFunc=new callFunc(ExpressionType.FUNC, function);
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
                //常量不能被赋值
                if(tmp.isConst){
                    throw new AnalyzeError(ErrorCode.AssignToConstant, token.getStartPos());
                }
                tmp.isInitialized = true;
                expect(TokenType.ASSIGN);
                exprToken[callFuncP].clear();
                analyseExpr();
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
            exprToken[callFuncP].add(new Uinteger(ExpressionType.UINT_LITERAL, (int)token.getValue()));
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
        }
        addSymbol(token.getValueString(),false,false,token.getStartPos(),1, typeAndPos);
        expect(TokenType.COLON);
        int varType = analyseTyParam();
        if(!check(TokenType.SEMICOLON)) {
            expect(TokenType.ASSIGN);
            exprToken[callFuncP].clear();
            analyseExpr();
            debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
            
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
            addSymbol(token.getValueString(),false,false,token.getStartPos(),0,typeAndPos);
        }
        //把新的func入栈
        cntParam = 0;
        cntLocal = 0;
        funcList[++funcP] = new Function(0,leftP,0,0,funcP, false);
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
            analyseExprStmt();
        }
    }

    /**
     * expr_stmt -> expr ';'
     */
    private void analyseExprStmt() throws CompileError{
        exprToken[callFuncP].clear();
        analyseExpr();
        debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
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
            exprToken[callFuncP].clear();
            analyseExpr();
            debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
            if(currentFunc.returnType != 1){
                throw new AnalyzeError(ErrorCode.InvalidReturnType, token.getStartPos());
            }
        }
        //如果没有表达式
        else{
            if(currentFunc.returnType != 2){
                throw new AnalyzeError(ErrorCode.InvalidReturnType, token.getStartPos());
            }
        }
        expect(TokenType.SEMICOLON);
    }

    /**
     * while_stmt -> 'while' expr block_stmt
     */
    private void analyseWhileStmt() throws CompileError{
        expect(TokenType.WHILE_KW);
        exprToken[callFuncP].clear();
        analyseExpr();
        debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
        analyseBlockStmt();
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
     */
    private void analyseIFStmt() throws CompileError {
        expect(TokenType.IF_KW);
        exprToken[callFuncP].clear();
        analyseExpr();
        debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
        analyseBlockStmt();
        while (check(TokenType.ELSE_KW)){
            expect(TokenType.ELSE_KW);
            if(check(TokenType.IF_KW)) {
                expect(TokenType.IF_KW);
                exprToken[callFuncP].clear();
                analyseExpr();
                debug_print.print_expr(MidToLast.midToLast(exprToken[callFuncP]), true);
                analyseBlockStmt();
            }
            else{
                analyseBlockStmt();
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
            addSymbol(token.getValueString(),true,true,token.getStartPos(),0, typeAndPos);
        }
        else{
            Token token = expect(TokenType.IDENT);
            typeAndPos = new TypeAndPos(3,cntParam);
            addSymbol(token.getValueString(),true,false,token.getStartPos(),0, typeAndPos);
        }
        varTop = stackLeft[leftP];
        expect(TokenType.COLON);
        varTop.type = analyseTyParam();
    }
}

