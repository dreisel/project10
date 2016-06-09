import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by daniel & ari on 26/05/16.
 *             304953243 201524089
 */
public class CompilationEngine {
    private Tokenizer tokenizer;
    private PrintWriter writer;
    private Set<String> types;
    private Token token;
    private VMWriter vmWriter;
    private SymbolTable symbolTable;
    private Map<SymbolTable.Kind,VMWriter.KeyWord> kindToSegment;
    private String currentClass = "";
    private String currentSubroutine = "";
    private int ifCounter = 0;
    private int whileCounter = 0;

    public CompilationEngine(File classFile, File outFile){
        try {
            this.tokenizer = new Tokenizer(new BufferedReader(new FileReader(classFile)));
            this.writer = new PrintWriter(outFile);
            this.vmWriter = new VMWriter(this.writer);
            this.symbolTable = new SymbolTable();
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        types = new HashSet<>();
        types.add("int");
        types.add("boolean");
        types.add("char");

        this.kindToSegment = new HashMap<>();
        this.kindToSegment.put(SymbolTable.Kind.FIELD, VMWriter.KeyWord.THIS);
        this.kindToSegment.put(SymbolTable.Kind.STATIC, VMWriter.KeyWord.STATIC);
        this.kindToSegment.put(SymbolTable.Kind.VAR, VMWriter.KeyWord.LOCAL);
        this.kindToSegment.put(SymbolTable.Kind.ARG, VMWriter.KeyWord.ARG);
    }

    /** COMPILATION **/


    //'class' className '{' classVarDec* subroutineDec* '}'
    public void compileClass(){

        advance();
        eat("class");
        currentClass = eatName();
        eat("{");
        //classVarDec*
        while (token.keyWord() == Tokenizer.KeyWord.STATIC || token.keyWord() == Tokenizer.KeyWord.FIELD) {
            compileClassVarDec();
        }
        //subroutineDec*
        while (!(token.type == Token.TokenType.SYMBOL && token.symbol() == '}')) {
            compileSubroutineDec();
        }
        if (tokenizer.hasMoreTokens()){
            throwError("Invalid class File");
        }
        eat("}");

        //save file
        writer.flush();
        writer.close();

    }
    //('static' | 'field') type varName (',' varNam)* ';'
    private void compileClassVarDec() {
        SymbolTable.Kind kind = eatKind();
        String type = eatType();
        String varName = eatName();

        symbolTable.insert(new VariableProperty(varName, type,kind, SymbolTable.Scope.CLASS));

        while(!(token.type == Token.TokenType.SYMBOL && token.symbol() == ';')) {
            eat(",");
            varName = eatName();
            symbolTable.insert(new VariableProperty(varName, type,kind, SymbolTable.Scope.CLASS));
        }
        eat(";"); // eat ;
    }

    //('constructor'|'function'|'method') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
    private void compileSubroutineDec(){
        this.ifCounter = 0;
        this.whileCounter = 0;
        Tokenizer.KeyWord keyWord  = token.keyWord();
        symbolTable.resetVarArg();
        if (keyWord == Tokenizer.KeyWord.METHOD){
            symbolTable.insert(new VariableProperty("this",currentClass, SymbolTable.Kind.ARG, SymbolTable.Scope.SUBROUTINE));
        }
        advance();
        if (types.contains(token.string) || token.string.equals("void") || token.type == Token.TokenType.IDENTIFIER){
            getTokenString();

        } else {
            throwError("Illegal type");
        }

        currentSubroutine = eatName();
        eat("(");
        compileParameterList();
        eat(")");
        compileSubroutineBody(keyWord);
    }
    // '{' varDec* statements '}'
    private void compileSubroutineBody(Tokenizer.KeyWord keyWord){
        eat("{");
        //varDec*
        while (token.keyWord() == Tokenizer.KeyWord.VAR) {
            compileVarDec();
        }
        //declare the function
        vmWriter.writeFunction(currentClass + "." + currentSubroutine,
                symbolTable.VarCount(SymbolTable.Kind.VAR));
        //constructor or method
        if (keyWord == Tokenizer.KeyWord.CONSTRUCTOR){
            // allocate memory
            vmWriter.writePush(VMWriter.KeyWord.CONST,symbolTable.VarCount(SymbolTable.Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.KeyWord.POINTER,0);
        } else if (keyWord == Tokenizer.KeyWord.METHOD){
            vmWriter.writePush(VMWriter.KeyWord.ARG, 0);
            vmWriter.writePop(VMWriter.KeyWord.POINTER,0);
        }

        compileStatements();
        eat("}");
    }
    // statement*
    private void compileStatements() {
        boolean statement = true;
        while (statement) {
            if(token.type == Token.TokenType.KEYWORD) {
                switch (token.keyWord()) {
                    case LET:
                        compileLet();
                        break;
                    case IF:
                        compileIf();
                        break;
                    case WHILE:
                        compileWhile();
                        break;
                    case DO:
                        compileDo();
                        break;
                    case RETURN:
                        compileReturn();
                        break;
                    default:
                        statement = false;
                        break;
                }
            } else {
                statement = false;
            }
        }
    }
    //'return' expression? ';'
    private void compileReturn() {
        eat("return");
        if(token.type != Token.TokenType.SYMBOL || token.symbol() != ';'){
            compileExpression();

        } else {
            //push 0 to stack when void
            vmWriter.writePush(VMWriter.KeyWord.CONST,0);
        }
        eat(";");
        vmWriter.writeReturn();
    }

    // 'do' subroutineCall ';'
    private void compileDo() {
        eat("do");
        compileSubroutineCall();
        eat(";");
        vmWriter.writePop(VMWriter.KeyWord.TEMP,0);
    }
    private String getLabel(String name){
        return name + ifCounter++;
    }
    //'while' '(' expression ')' '{' statements '}'
    private void compileWhile() {
        String loopStart = "WHILE_EXP" + whileCounter;
        String loopEnd = "WHILE_END" + whileCounter++;
        vmWriter.writeLabel(loopStart);
        eat("while");
        eat("(");
        compileExpression();
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(loopEnd);
        eat(")");
        eat("{");
        compileStatements();
        eat("}");
        vmWriter.writeGoto(loopStart);
        vmWriter.writeLabel(loopEnd);
    }

    //'if' '(' expression ')' '{' statements '}'('else' '{'statements '}')?
    private void compileIf() {
        boolean hasElse;
        String ifTrue = "IF_TRUE" + ifCounter;
        String ifEnd = "IF_END" + ifCounter;
        String ifFalse = "IF_FALSE" + ifCounter++;
        eat("if");
        eat("(");
        compileExpression();
        vmWriter.writeIf(ifTrue);
        vmWriter.writeGoto(ifFalse);
        vmWriter.writeLabel(ifTrue);
        eat(")");
        eat("{");
        compileStatements();
        eat("}");
        hasElse = (token.keyWord() == Tokenizer.KeyWord.ELSE);
        if(hasElse)
            vmWriter.writeGoto(ifEnd);
        vmWriter.writeLabel(ifFalse);
        if(hasElse) {
            eat("else");
            eat("{");
            compileStatements();
            eat("}");
        }
        if(hasElse)
            vmWriter.writeLabel(ifEnd);
    }

    //'let' varName ('[' expression ']')? '=' expression ';'
    private void compileLet() {
        eat("let");
        String name = eatName();
        boolean array = false;
        if(token.type == Token.TokenType.SYMBOL && token.symbol() == '['){
            eat("[");
            //pushing base array address
            compileExpression();
            vmWriter.writePush(kindToSegment.get(symbolTable.KindOf(name)),symbolTable.IndexOf(name));
            vmWriter.writeArithmetic("add");
            eat("]");
            array = true;
        }
        eat("=");
        compileExpression();
        eat(";");
        if (array){
            //arr[i] = expression
            vmWriter.writePop(VMWriter.KeyWord.TEMP,0);
            vmWriter.writePop(VMWriter.KeyWord.POINTER,1);
            vmWriter.writePush(VMWriter.KeyWord.TEMP,0);
            vmWriter.writePop(VMWriter.KeyWord.THAT,0);
        }else {
            //var = expression
            vmWriter.writePop(kindToSegment.get(symbolTable.KindOf(name)), symbolTable.IndexOf(name));
        }
    }

    //integerConstant | stringConstant | keywordConstant | varName |
    // vrName'[' expression ']' | subroutineCall | unaryOp term | '(' expression ')'
    private void compileTerm(){
        //integerConstant | stringConstant | keywordConstant
        if (token.integerConstant){
            //integerConstant
            vmWriter.writePush(VMWriter.KeyWord.CONST,token.intVal());
            advance();
        }
        else if(token.stringConstant){
            //stringConstant new a string and append every char to the new stack
            String str = getTokenString();
            //push str length for String constructor
            vmWriter.writePush(VMWriter.KeyWord.CONST,str.length());
            vmWriter.writeCall("String.new",1);
            for (int i = 0; i < str.length(); i++){
                vmWriter.writePush(VMWriter.KeyWord.CONST,(int)str.charAt(i));
                vmWriter.writeCall("String.appendChar",2);
            }
        }else if( token.keywordConstant ){
            switch (token.keyWord()){
                case FALSE:
                    vmWriter.writePush(VMWriter.KeyWord.CONST,0);
                    break;
                case NULL:
                    vmWriter.writePush(VMWriter.KeyWord.CONST,0);
                    break;
                case TRUE:
                    vmWriter.writePush(VMWriter.KeyWord.CONST,0);
                    vmWriter.writeArithmetic("not");
                    break;
                case THIS:
                    vmWriter.writePush(VMWriter.KeyWord.POINTER,0);
                    break;
            }
            advance();
        } else if (token.varName){
            Token next = tokenizer.lookAhead();
            if (next.type == Token.TokenType.SYMBOL && next.symbol() == '['){
                //varName '[' expression ']'
                String varName = getTokenString();
                //pushing base array address
                eat("[");
                compileExpression();
                vmWriter.writePush(kindToSegment.get(symbolTable.KindOf(varName)),symbolTable.IndexOf(varName));
                eat("]");
                //push arr[i]
                vmWriter.writeArithmetic("add");
                vmWriter.writePop(VMWriter.KeyWord.POINTER,1);
                vmWriter.writePush(VMWriter.KeyWord.THAT,0);

            }else if (next.type == Token.TokenType.SYMBOL && (next.symbol() == '.' || next.symbol() == '(')){
                //subroutineCall
                compileSubroutineCall();
            }else {
                //varName
                String varName = eatName();
                vmWriter.writePush(kindToSegment.get(symbolTable.KindOf(varName)),symbolTable.IndexOf(varName));
            }
        } else if (token.unaryOp){
            // unaryOp term
            char unaryOp = token.symbol();
            advance();
            compileTerm();
            if (unaryOp == '-'){
                vmWriter.writeArithmetic("neg");
            }else {
                vmWriter.writeArithmetic("not");
            }
        } else {
            // '(' expression? ')'
            eat("(");
            if(!(token.type == Token.TokenType.SYMBOL && token.symbol() == ')')) {
                compileExpression();
            }
            eat(")");
        }
    }
    // subroutineName '(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
    private void compileSubroutineCall() {
        String name = eatName();
        int nArgs = 0;
        if(token.type == Token.TokenType.SYMBOL && token.symbol() == '.'){
            //(className|varName) '.' subroutineName '(' expressionList ')'
            eat(".");
            String subroutineName = eatName();

            String type = symbolTable.typeOf(name);

            if (type == null){
                name = name + "." + subroutineName;
            }else {
                nArgs++;
                //push var to stack (this)
                vmWriter.writePush(kindToSegment.get(symbolTable.KindOf(name)),symbolTable.IndexOf(name));
                name = type + "." + subroutineName;
            }

        } else {
            //push this
            vmWriter.writePush(VMWriter.KeyWord.POINTER,0);
            nArgs ++;
            name = currentClass + '.' + name;

        }
        eat("(");
        nArgs += compileExpressionList();
        eat(")");
        vmWriter.writeCall(name, nArgs);
    }
    // (expression (',' expression)*)?
    private int compileExpressionList() {
        int count = 0;
        while (!(token.type == Token.TokenType.SYMBOL && token.symbol() == ')')){
            compileExpression();
            count++;
            if(token.type == Token.TokenType.SYMBOL && token.symbol() == ',' ){
                eat(",");
            }
        }
        return count;
    }
    //term (op term)*
    private void compileExpression(){

        compileTerm();

        while (token.op) {
            // (op term)+
            String op = vmWriter.opToVM.get(token.symbol());
            advance();
            compileTerm();
            vmWriter.writeArithmetic(op);
        }
    }
    //'var' type varName (',' varName)* ';'
    private void compileVarDec() {
        eat("var");
        String type = eatType();
        String name = eatName();
        symbolTable.insert(new VariableProperty(name,type, SymbolTable.Kind.VAR, SymbolTable.Scope.SUBROUTINE));
        while (!(token.type == Token.TokenType.SYMBOL && token.symbol() == ';')){
            eat(",");
            name = eatName();
            symbolTable.insert(new VariableProperty(name,type, SymbolTable.Kind.VAR, SymbolTable.Scope.SUBROUTINE));
        }
        eat(";");
    }
    //( (type varName) (',' type varName)*)?
    private void compileParameterList(){
        if(!(token.type == Token.TokenType.SYMBOL && token.symbol() == ')')) {
            String type = eatType();
            String name = eatName();
            symbolTable.insert(new VariableProperty(name,type, SymbolTable.Kind.ARG, SymbolTable.Scope.SUBROUTINE));
            while (!(token.type == Token.TokenType.SYMBOL && token.symbol() == ')')) {
                eat(",");
                type = eatType();
                name = eatName();
                symbolTable.insert(new VariableProperty(name,type, SymbolTable.Kind.ARG, SymbolTable.Scope.SUBROUTINE));
            }
        }
    }


    /** UTILS **/

    //consume a type token. verify and print
    private String eatType(){
        if(types.contains(token.string) || token.type == Token.TokenType.IDENTIFIER) {
            return getTokenString();
        } else
           throwError("Illegal var Type");

        return null;
    }
    private SymbolTable.Kind eatKind() {
        return symbolTable.stringTokind.get(getTokenString());
    }
    //prints the current token and advances to the next
    private String getTokenString() {
        String tokenStr = token.string;
        advance();
        return tokenStr;
    }
    //consume a name token. verify and print
    private String eatName(){
        if (!token.varName){
            throwError("Illegal subroutine identifier");
        }
        return getTokenString();
    }
    //throws input as error
    private void throwError(String s) {
        throw new RuntimeException(s);
    }
    //consume  the token input. verify and print
    private void eat(String string) {
        if (tokenizer.currentToken.string.equals(string)) {
            getTokenString();
        } else
            throw new RuntimeException("compilation Error!");
    }
    //write to file
    private void write(String s){
        writer.println(s);
    }
    // set token to be the next token from the tokenizer
    private void advance() {
        if(tokenizer.hasMoreTokens()) {
            tokenizer.advance();
            token = tokenizer.currentToken;
        }
    }




}
