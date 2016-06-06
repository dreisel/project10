import java.io.BufferedReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by daniel & ari on 26/05/16.
 *             304953243 201524089
 */
public class Tokenizer {

    public enum KeyWord {
        CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN,
        CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE,
        WHILE, RETURN, TRUE, FALSE, NULL, THIS
    }

    private BufferedReader reader;
    public Token currentToken;
    private final Character[] symbols = {'{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>', '=', '~'};
    private  final Set<Character> symbolSet = new HashSet<>(Arrays.asList(symbols));
    private final String[] keywords = {"class", "method", "function", "constructor", "int", "boolean",
            "char", "void", "var", "static", "field", "let", "do", "if", "else",
            "while", "return", "true", "false", "null", "this"};
    private final Set<String> keywordSet = new HashSet<>(Arrays.asList(keywords));
    public static Map<String,KeyWord> keyMap;
    private List<Token> tokensList;
    private int tokenIndex;

    public Tokenizer(BufferedReader reader) throws Exception {
        this.reader = reader;
        this.tokenIndex = -1;
        fillTokens();
    }

    //read the file and fill the token array
    private void fillTokens() throws Exception{
        tokensList = new ArrayList<>();
        String regex = "([a-zA-Z_][a-zA-Z_0-9]*)" + "|" +
                "([\\{\\}\\(\\)\\[\\]\\.;\\+\\-\\*/&\\|<>=,~])" + "|" +
                "([0-9]+)" + "|" +
                "\"([^\"\n]*)\"";
        String fileAsString = "";
        Pattern pattern = Pattern.compile(regex);
        String line = reader.readLine();
        while (line != null) {
            if(line.contains("//")){
                line = line.substring(0,line.indexOf("//"));
            }
            fileAsString += line + "\n";
            line = reader.readLine();
        }
        //remove comments!
        fileAsString = removeComments(fileAsString);
        //remove empty lines
        fileAsString = fileAsString.replaceAll("\n", "");
        Matcher matcher = pattern.matcher(fileAsString);
        while (matcher.find()) {
            Token token = null;
            if(matcher.group(1) != null) {
                String tokenStr = matcher.group(1);
                if(keywordSet.contains(tokenStr)){
                    token = new Token(tokenStr, Token.TokenType.KEYWORD);
                } else {
                    token = new Token(tokenStr, Token.TokenType.IDENTIFIER);
                }
            }  else if(matcher.group(2) != null){
                token = new Token(matcher.group(2),Token.TokenType.SYMBOL);
            } else if(matcher.group(3) != null){
                token = new Token(matcher.group(3),Token.TokenType.INT_CONST);
            } else if(matcher.group(4) != null){
                token = new Token(matcher.group(4),Token.TokenType.STRING_CONST);
            }
            tokensList.add(token);
        }

    }
    //remove comments from file
    public static String removeComments(String fileAsString){
        if (!fileAsString.contains("/*")){
            return fileAsString;
        }

        int blockStart = fileAsString.indexOf("/*");
        int blockEnd = fileAsString.indexOf("*/");

        while(blockStart != -1){

//            if (endIndex == -1){
//                return fileAsString.substring(0,blockStart - 1);
//            }

            fileAsString = fileAsString.substring(0,blockStart) + fileAsString.substring(blockEnd + 2);
            blockStart = fileAsString.indexOf("/*");
            blockEnd = fileAsString.indexOf("*/");
        }

        return fileAsString;
    }
    //advances to the next token
    public void advance() {
        tokenIndex++;
        currentToken = tokensList.get(tokenIndex);
    }
    // return if there are more tokens available
    public boolean hasMoreTokens(){
        return (tokenIndex < tokensList.size() - 1);
    }
    // return the next token without advancing
    public Token lookAhead(){
        return tokensList.get(tokenIndex + 1);
    }
    //fill static key map
    static  {
        keyMap = new HashMap<>();
        keyMap.put("class",KeyWord.CLASS);
        keyMap.put("method",KeyWord.METHOD);
        keyMap.put("function",KeyWord.FUNCTION);
        keyMap.put("constructor",KeyWord.CONSTRUCTOR);
        keyMap.put("int",KeyWord.INT);
        keyMap.put("boolean",KeyWord.BOOLEAN);
        keyMap.put("char",KeyWord.CHAR);
        keyMap.put("void",KeyWord.VOID);
        keyMap.put("var",KeyWord.VAR);
        keyMap.put("static",KeyWord.STATIC);
        keyMap.put("field",KeyWord.FIELD);
        keyMap.put("let",KeyWord.LET);
        keyMap.put("do",KeyWord.DO);
        keyMap.put("if",KeyWord.IF);
        keyMap.put("else",KeyWord.ELSE);
        keyMap.put("while",KeyWord.WHILE);
        keyMap.put("return",KeyWord.RETURN);
        keyMap.put("true",KeyWord.TRUE);
        keyMap.put("false",KeyWord.FALSE);
        keyMap.put("null",KeyWord.NULL);
        keyMap.put("this",KeyWord.THIS);
    }



}
