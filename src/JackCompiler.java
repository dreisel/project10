import java.io.File;

/**
 * Created by daniel & ari on 26/05/16.
 *             304953243 201524089
 */
public class JackCompiler {
    public static void main(String[] args) {
        try {
            // write your code here
            File[] files;
            File in = new File(args[0]);
            if(in.isDirectory()){
                files = in.listFiles();
            } else {
                files = new File[1];
                files[0] = in;
            }

            for(File file : files){
                if(file.getName().endsWith(".jack")){
                    File out = new File(file.getAbsolutePath().replace(".jack",".vm"));
                    CompilationEngine compilationEngine = new CompilationEngine(file,out);
                    compilationEngine.compileClass();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
