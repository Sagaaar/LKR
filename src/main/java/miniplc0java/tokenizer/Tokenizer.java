package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.util.Pos;

import javax.lang.model.type.TypeKind;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }
        //查看下一个字符，但是不移动指针
        char peek = it.peekChar();
        //数字
        if (Character.isDigit(peek)) {
            return lexUInt();
        }
        //字母
        else if (Character.isAlphabetic(peek) || it.peekChar()=='_') {
            return lexIdentOrKeyword();
        }
        else if(peek == '"'){
            return lexString();
        }
        else {
            return lexOperatorOrUnknown();
        }
    }
    //无符号整数判断
    private Token lexUInt() throws TokenizeError {
        Pos now = it.currentPos();
        StringBuffer buffer = new StringBuffer();
        buffer.append(it.peekChar());
        it.nextChar();
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        while(Character.isDigit(it.peekChar())){
            buffer.append(it.peekChar());
            it.nextChar();
        }
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        try{
            Integer res =Integer.parseInt(buffer.toString());
            // Token 的 Value 应填写数字的值
            return new Token(TokenType.UINT_LITERAL, res, now, it.currentPos());
        }catch (Exception e){
            throw new TokenizeError(ErrorCode.ExpectedToken, now);
        }
    }
    //关键字&标识符判断
    private Token lexIdentOrKeyword() throws TokenizeError {
        Pos now = it.ptr;
        StringBuffer buffer = new StringBuffer();
        buffer.append(it.peekChar());
//        System.out.println(buffer.toString());
        it.nextChar();
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        while(Character.isAlphabetic(it.peekChar()) || Character.isDigit(it.peekChar()) || it.peekChar()=='_'){
            buffer.append(it.peekChar());
            it.nextChar();
//            System.out.println(buffer.toString());
        }
//        System.out.println(buffer.toString());
        try{
            TokenType tokentype = whichKeyWord(buffer.toString());
            if(tokentype!= TokenType.None){
                // 尝试将存储的字符串解释为关键字
                // -- 如果是关键字，则返回关键字类型的 token
                // Token 的 Value 应填写标识符或关键字的字符串
                return new Token(tokentype, buffer.toString(), now, it.currentPos());
            }
            else{
                // -- 否则，返回标识符
                return new Token(TokenType.IDENT, buffer.toString(), now, it.currentPos());
            }
        }catch (Exception e){
            throw new TokenizeError(ErrorCode.ExpectedToken, now);
        }
    }
    //String判断
    private Token lexString() throws TokenizeError{
        Pos now = it.currentPos();
        StringBuffer buffer = new StringBuffer();
        buffer.append('"');
        it.nextChar();
        boolean pre = false;
        while(!it.isEOF()){
            if(pre==false && it.peekChar()=='"'){
                buffer.append(it.peekChar());
                it.nextChar();
                break;
            }
            if(it.peekChar() == '\\' && pre==false){
                pre = true;
            }
            else{
                pre = false;
            }
            buffer.append(it.peekChar());
            it.nextChar();
        }
        String pattern="\"(([^\"\\\\])|(\\\\[\\\\\"'nrt]))*\"";
        if(buffer.toString().matches(pattern)){
            return new Token(TokenType.STRING_LITERAL,buffer.toString(),now, it.currentPos());
        }
        else{
            throw new TokenizeError(ErrorCode.StringError, now);
        }
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        Pos now = it.currentPos();
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':
                if(it.peekChar() == '>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());

            case '*':
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            case '/':
                if(it.peekChar() == '/'){
                    it.nextChar();
                    while(!it.isEOF()){
                        if(it.peekChar()=='\n'){
                            it.nextChar();
                            break;
                        }
                        it.nextChar();
                    }
                    return new Token(TokenType.COMMENT, "//", now, it.currentPos());
                }
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());

            case '=':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.ASSIGN, "=", it.previousPos(), it.currentPos());

            case '!':
                if(it.peekChar() == '!') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }

            case '<':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());

            case '>':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());

            case '(':
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());

            case ')':
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());

            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());

            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());

            case ',':
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());

            case ':':
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());

            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());

            default:
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    private TokenType whichKeyWord(String word){
        if(word.equals("fn")){
            return TokenType.FN_KW;
        }
        else if(word.equals("let")){
            return TokenType.LET_KW;
        }
        else if(word.equals("const")){
            return TokenType.CONST_KW;
        }
        else if(word.equals("as")){
            return TokenType.AS_KW;
        }
        else if(word.equals("while")){
            return TokenType.WHILE_KW;
        }
        else if(word.equals("if")){
            return TokenType.IF_KW;
        }
        else if(word.equals("else")){
            return TokenType.ELSE_KW;
        }
        else if(word.equals("return")){
            return TokenType.RETURN_KW;
        }
        else if(word.equals("int")){
            return TokenType.INTEGER;
        }
        else if(word.equals("void")){
            return TokenType.VOID;
        }
        else{
            return TokenType.None;
        }
    }
}