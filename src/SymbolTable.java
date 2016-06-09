import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel & ari on 26/05/16.
 *             304953243 201524089
 */
public class SymbolTable {

    enum Kind {
        STATIC,FIELD,ARG,VAR
    }

    enum Scope{
        CLASS,SUBROUTINE
    }

    public Map<String,Kind> stringTokind;
    private Map<String,VariableProperty> classSymbols;
    private Map<String,VariableProperty> subroutineSymbols;
    private Map<Kind,Integer> kindCount;


    public SymbolTable() {
        classSymbols = new HashMap<>();
        subroutineSymbols = new HashMap<>();

        stringTokind = new HashMap<>();
        kindCount = new HashMap<>();
        kindCount.put(Kind.STATIC,0);
        kindCount.put(Kind.FIELD,0);
        kindCount.put(Kind.ARG,0);
        kindCount.put(Kind.VAR,0);

        stringTokind.put("static",Kind.STATIC);
        stringTokind.put("field",Kind.FIELD);
        stringTokind.put("arg",Kind.ARG);
        stringTokind.put("var",Kind.VAR);

    }

    public int VarCount(Kind kind){
        return kindCount.get(kind);
    }

    public Kind KindOf(String name){
        VariableProperty var = subroutineSymbols.get(name);
        if(var == null)
            var = classSymbols.get(name);
        if(var == null)
            return null;
        return var.getKind();
    }
    public String typeOf(String name){
        VariableProperty var = subroutineSymbols.get(name);
        if(var == null)
            var = classSymbols.get(name);
        if(var == null)
            return null;
        return var.getType();
    }

    public int IndexOf(String name){
        VariableProperty var = subroutineSymbols.get(name);
        if(var == null)
            var = classSymbols.get(name);
        if(var == null)
            return -1;
        return var.getIndex();
    }

    public void insert(VariableProperty var){
        var.setIndex(kindCount.get(var.getKind()));
        kindCount.put(var.getKind(),var.getIndex() + 1);
        switch (var.getScope()){
            case CLASS:
                classSymbols.put(var.getName(),var);
                break;
            case SUBROUTINE:
                subroutineSymbols.put(var.getName(),var);
                break;
        }
    }

    public void resetVarArg(){
        subroutineSymbols.clear();
        kindCount.put(Kind.VAR,0);
        kindCount.put(Kind.ARG,0);
    }
}
