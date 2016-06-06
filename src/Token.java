
/**
 * Created by daniel & ari on 26/05/16.
 *             304953243 201524089
 */

public class Token {
    String string;
    TokenType type;
    boolean op;
    boolean unaryOp;
    boolean integerConstant;
    boolean stringConstant;
    boolean keywordConstant;
    boolean varName;

    public Token(String string, TokenType type) {
        this.string = string;
        this.type = type;
        this.op = ("+-*/&|<>=".indexOf(string.charAt(0)) >= 0);
        this.unaryOp = (string.charAt(0) == '-' ||string.charAt(0) == '~');
        this.integerConstant = (type == TokenType.INT_CONST);
        this.stringConstant = (type == TokenType.STRING_CONST);
        this.keywordConstant = (string.equals("true") ||string.equals("false") ||
                                string.equals("null") ||string.equals("this")  );
        this.varName = (type == TokenType.IDENTIFIER);

    }

    public enum TokenType {
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST;
    }
    public char symbol(){
        return this.string.charAt(0);
    }
    public int intVal(){
        return Integer.parseInt(this.string);
    }
    public String stringVal(){
        return this.string.trim();
    }
    public String identifier(){
        return this.string;
    }
    public Tokenizer.KeyWord keyWord(){
        return Tokenizer.keyMap.get(this.string);
    }
}
