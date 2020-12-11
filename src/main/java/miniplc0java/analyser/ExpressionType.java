package miniplc0java.analyser;

public enum ExpressionType {
    /** 加号*/
    PLUS,
    /** 减号 */
    MINUS,
    /** 负号*/
    NEG,
    /** 乘号 */
    MUL,
    /** 除号 */
    DIV,
    /** 左括号 */
    L_PAREN,
    /** 右括号 */
    R_PAREN,
    /** 变量*/
    Var,
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
    /** 函数*/
    FUNC,
    /** 无符号整数*/
    UINT_LITERAL;
    @Override
    public String toString() {
        switch (this) {
            case UINT_LITERAL:
                return "UintNum";
            case PLUS:
                return "+";
            case MINUS:
            case NEG:
                return "-";
            case MUL:
                return "*";
            case DIV:
                return "/";
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
            case Var:
                return "Var";
            case FUNC:
                return "Func";
            default:
                return "InvalidToken";
        }
    }
}