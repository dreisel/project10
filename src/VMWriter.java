import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 01/06/16.
 */
public class VMWriter {
    PrintWriter writer;
    Map<KeyWord, String> keywordToVm;
    enum KeyWord{
        CONST,ARG,LOCAL,STATIC,THIS,THAT,POINTER,TEMP
    }
    public Map<Character,String> opToVM;
    public VMWriter(PrintWriter writer) {
        this.writer = writer;
        keywordToVm = new HashMap<>();
        keywordToVm.put(KeyWord.CONST,"constant");
        keywordToVm.put(KeyWord.ARG,"argument");
        keywordToVm.put(KeyWord.LOCAL,"local");
        keywordToVm.put(KeyWord.STATIC,"static");
        keywordToVm.put(KeyWord.THIS,"this");
        keywordToVm.put(KeyWord.THAT,"that");
        keywordToVm.put(KeyWord.POINTER,"pointer");
        keywordToVm.put(KeyWord.TEMP,"temp");

        opToVM = new HashMap<>();
        opToVM.put('+',"add");
        opToVM.put('-',"sub");
        opToVM.put('*',"call Math.multiply 2");
        opToVM.put('/',"call Math.divide 2");
        opToVM.put('<',"lt");
        opToVM.put('>',"gt");
        opToVM.put('=',"eq");
        opToVM.put('&',"and");
        opToVM.put('|',"or");
    }

    public void writePush(KeyWord segment, int index) {
        write("push",keywordToVm.get(segment),index+"");
    }
    public void writePop(KeyWord segment, int index) {
        write("pop",keywordToVm.get(segment),index + "");
    }
    public void writeArithmetic(String command) {
        write(command,"","");
    }
    public void writeLabel(String label) {
        write("label", label,"");
    }
    public void writeGoto(String label){
        write("goto", label,"");
    }
    public void writeIf(String label){
        write("if-goto", label,"");
    }
    public void writeCall(String name, int nArgs){
        write("call",name,nArgs + "");
    }
    public void writeFunction(String name, int nLocals){
        write("function", name,nLocals + "");
    }
    public void writeReturn(){
        write("return","","");
    }

    private void write(String command,String type, String index){
        writer.println(command +" " + type + " " + index);
        writer.flush();
    }

}
