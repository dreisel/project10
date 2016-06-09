/**
 * Created by daniel & ari on 26/05/16.
 *             304953243 201524089
 */
public class VariableProperty {

    private String name;
    private String type;
    private SymbolTable.Kind kind;
    private SymbolTable.Scope scope;
    private int index;


    public VariableProperty(String name, String type, SymbolTable.Kind kind, SymbolTable.Scope scope) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.scope = scope;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SymbolTable.Kind getKind() {
        return kind;
    }

    public void setKind(SymbolTable.Kind kind) {
        this.kind = kind;
    }

    public SymbolTable.Scope getScope() {
        return scope;
    }

    public void setScope(SymbolTable.Scope scope) {
        this.scope = scope;
    }
}
