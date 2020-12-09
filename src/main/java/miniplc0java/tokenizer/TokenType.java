package miniplc0java.tokenizer;

public enum TokenType {
    /** 空 */
    None,
    /** int类型*/
    INTEGER,
    /** void类型*/
    VOID,
    /** 无符号整数 */
    UINT_LITERAL,
    /** 字符串 */
    STRING_LITERAL,
    /** 标识符 */
    IDENT,
    /** 函数 */
    FN_KW,
    /** Let */
    LET_KW,
    /** AS */
    AS_KW,
    /** Const */
    CONST_KW,
    /** WHILE */
    WHILE_KW,
    /** IF */
    IF_KW,
    /** ELSE */
    ELSE_KW,
    /** RETURN */
    RETURN_KW,
    /** 加号 */
    PLUS,
    /** 减号 */
    MINUS,
    /** 乘号 */
    MUL,
    /** 除号 */
    DIV,
    /** 赋值等号 */
    ASSIGN,
    /** 等号 */
    EQ,
    /** 不等号 */
    NEQ,
    /** 小于号 */
    LT,
    /** 大于号 */
    GT,
    /** 小于等于号 */
    LE,
    /** 大于等于号 */
    GE,
    /** 分号 */
    SEMICOLON,
    /** 左括号 */
    L_PAREN,
    /** 右括号 */
    R_PAREN,
    /** 左大括号 */
    L_BRACE,
    /** 右大括号 */
    R_BRACE,
    /** 箭头 */
    ARROW,
    /** 逗号 */
    COMMA,
    /** 冒号 */
    COLON,
    /**注释**/
    COMMENT,
    /** 文件尾 */
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case None:
                return "NullToken";
            case FN_KW:
                return "fn";
            case LET_KW:
                return "let";
            case CONST_KW:
                return "const";
            case AS_KW:
                return "as";
            case WHILE_KW:
                return "while";
            case IF_KW:
                return "if";
            case ELSE_KW:
                return "else";
            case RETURN_KW:
                return "return";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case MUL:
                return "*";
            case DIV:
                return "/";
            case ASSIGN:
                return "=";
            case EQ:
                return "==";
            case NEQ:
                return "!=";
            case LT:
                return "<";
            case GT:
                return ">";
            case LE:
                return "<=";
            case GE:
                return ">=";
            case L_PAREN:
                return "(";
            case R_PAREN:
                return ")";
            case L_BRACE:
                return "{";
            case R_BRACE:
                return "}";
            case ARROW:
                return "->";
            case COMMA:
                return ",";
            case COLON:
                return ":";
            case SEMICOLON:
                return ";";
            case IDENT:
                return "Ident";
            case STRING_LITERAL:
                return "String";
            case UINT_LITERAL:
                return "digit+";
            case VOID:
                return "void";
            case INTEGER:
                return "integer";
            case EOF:
                return "EOF";
            default:
                return "InvalidToken";
        }
    }
}
