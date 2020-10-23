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
        if (Character.isDigit(peek)) {
            return lexUInt();
        } else if (Character.isAlphabetic(peek)) {
            return lexIdentOrKeyword();
        } else {
            return lexOperatorOrUnknown();
        }
    }
    //无符号整数判断
    private Token lexUInt() throws TokenizeError {
        Pos now = it.ptr;
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
            return new Token(TokenType.Uint, res, it.previousPos(), it.currentPos());
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
        while(Character.isAlphabetic(it.peekChar()) || Character.isDigit(it.peekChar())){
            buffer.append(it.peekChar());
            it.nextChar();
//            System.out.println(buffer.toString());
        }
//        System.out.println(buffer.toString());
        try{
            TokenType tokentype = whichKeepWord(buffer.toString());
            if(tokentype!= TokenType.None){
                // 尝试将存储的字符串解释为关键字
                // -- 如果是关键字，则返回关键字类型的 token
                // Token 的 Value 应填写标识符或关键字的字符串
                return new Token(tokentype, buffer.toString(), it.previousPos(), it.currentPos());
            }
            else{
                // -- 否则，返回标识符
                return new Token(TokenType.Ident, buffer.toString(), it.previousPos(), it.currentPos());
            }
        }catch (Exception e){
            throw new TokenizeError(ErrorCode.ExpectedToken, now);
        }
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.Plus, '+', it.previousPos(), it.currentPos());

            case '-':
                return new Token(TokenType.Minus, '-', it.previousPos(), it.currentPos());

            case '*':
                return new Token(TokenType.Mult, '*', it.previousPos(), it.currentPos());

            case '/':
                return new Token(TokenType.Div, '/', it.previousPos(), it.currentPos());

            case '=':
                return new Token(TokenType.Equal, '=', it.previousPos(), it.currentPos());

            case ';':
                return new Token(TokenType.Semicolon, ';', it.previousPos(), it.currentPos());

            case '(':
                return new Token(TokenType.LParen, '(', it.previousPos(), it.currentPos());

            case ')':
                return new Token(TokenType.RParen, ')', it.previousPos(), it.currentPos());

            default:
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    private TokenType whichKeepWord(String word){
        if(word.equals(TokenType.Begin)){
            return TokenType.Begin;
        }
        else if(word.equals(TokenType.End)){
            return TokenType.End;
        }
        else if(word.equals(TokenType.Var)){
            return TokenType.Var;
        }
        else if(word.equals(TokenType.Const)){
            return TokenType.Const;
        }
        else if(word.equals(TokenType.Print)){
            return TokenType.Print;
        }
        else{
            return TokenType.None;
        }
    }
}