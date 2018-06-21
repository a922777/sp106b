import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Assembler {
    public static HashMap<String,Integer> cMap = new HashMap<String, Integer>();
    public static HashMap<String,String> compAMap = new HashMap<String, String>();
    public static HashMap<String,String> compMMap = new HashMap<String, String>();
    public static HashMap<String,String> dstMap = new HashMap<String, String>();
    public static HashMap<String,String> jmpMap = new HashMap<String, String>();

    static {
        //symTable
        cMap.put("SP",0);cMap.put("LCL",1);cMap.put("ARG",2);cMap.put("THIS",3);
        cMap.put("THAT",4);cMap.put("R0",0);cMap.put("R1",1);cMap.put("R2",2);
        cMap.put("R3",3);cMap.put("R4",4);cMap.put("R5",5);cMap.put("R6",6);
        cMap.put("R7",7);cMap.put("R8",8);cMap.put("R9",9);cMap.put("R10",10);
        cMap.put("R11",11);cMap.put("R12",12);cMap.put("R13",13);cMap.put("R14",14);
        cMap.put("R15",15);cMap.put("SCREEN",16384);cMap.put("KBD",24576);

        //comptable
        compAMap.put("0","101010");compAMap.put("1","111111");compAMap.put("-1","111010");
        compAMap.put("D","001100");compAMap.put("A","110000");compAMap.put("!D","001101");
        compAMap.put("!A","110001");compAMap.put("-D","001111");compAMap.put("-A","110011");
        compAMap.put("D+1","011111");compAMap.put("A+1","110111");compAMap.put("D-1","001110");
        compAMap.put("A-1","110010");compAMap.put("D+A","000010");compAMap.put("D-A","010011");
        compAMap.put("A-D","000111");compAMap.put("D&A","000000");compAMap.put("D|A","010101");
        compMMap.put("M","110000");compMMap.put("!M","110001");compMMap.put("-M","110011");
        compMMap.put("M+1","110111");compMMap.put("M-1","110010");compMMap.put("D+M","000010");
        compMMap.put("D-M","010011");compMMap.put("M-D","000111");compMMap.put("D&M","000000");
        compMMap.put("D|M","010101");

        //desttable
        dstMap.put("","000");dstMap.put("M","001");dstMap.put("D","010");dstMap.put("MD","011");
        dstMap.put("A","100");dstMap.put("AM","101");dstMap.put("AD","110");dstMap.put("AMD","111");

        //jumptable
        jmpMap.put("","000");jmpMap.put("JGT","001");jmpMap.put("JEQ","010");jmpMap.put("JGE","011");
        jmpMap.put("JLT","100");jmpMap.put("JNE","101");jmpMap.put("JLE","110");jmpMap.put("JMP","111");
    }

    public static HashMap<String,Integer> findLabels(String codes){

        HashMap<String,Integer> labels = new HashMap<String, Integer>();
        Scanner scan = new Scanner(codes);
        String line = "";
        int pc = 0;
        Pattern p = Pattern.compile("^\\([^0-9][0-9A-Za-z\\_\\:\\.\\$]+\\)$");
        Matcher m =null;

        while (scan.hasNextLine()){

            line = scan.nextLine();

            m = p.matcher(line);

            //Ｌ指令
            if (m.find()){

                labels.put(m.group().substring(1,m.group().length()-1), pc);

            }else {

                pc++;

            }

        }

        return labels;
    }


    public static String asmToHack(String codes){

        Scanner scan = new Scanner(codes);

        int addressDec = 0,//value of @value
            pc = 0,//pc count
            lineNumber = 0,//record lineNumber for exception
            startAddress = 16,//start address for variable
            temp = 0,//temp var
            flag1 = -1,//flag for "="
            flag2 = -1;//flag for ";"

        String line = "",//line read from Scanner
               varName = "",//value name of @value
               value = "",//for A instruction, 0+value
               a = "",//for C instruction, 111+a+comp+dst+jmp
               dst = "",
               comp = "",
               jmp = "",
               instructions = "";


        Pattern p = Pattern.compile("^[^0-9][0-9A-Za-z\\_\\:\\.\\$]+");
        Pattern p = Pattern.compile("^[^0-9][0-9A-Za-z\\_\\:\\.\\$]+");
        //dot (.), dollar sign ($), and colon (:) 不是數字開頭的

        Pattern pL = Pattern.compile("^\\([^0-9][0-9A-Za-z\\_\\:\\.\\$]+\\)$");

        HashMap<String,Integer> labels = findLabels(codes);

        HashMap<String,Integer> symbols = new HashMap<String, Integer>();

        while (scan.hasNextLine()){

            lineNumber++;

            line = scan.nextLine();

            if (line.charAt(0) == '@'){
                //Ａ指令

                varName = line.substring(1);

              
                if (labels.containsKey(varName)){

                    value = Parser.padLeftZero(Integer.toBinaryString(labels.get(varName)),15);

                }else {

                
                    if (varName.matches("[0-9]+")) {

                        value = Parser.padLeftZero(Integer.toBinaryString(Integer.parseInt(varName)), 15);

                    } else {
                       

                        if (cMap.containsKey(varName)){

                            value = Parser.padLeftZero(Integer.toBinaryString(cMap.get(varName)), 15);

                        }else {


                            if (p.matcher(varName).find()) {

                                //轉二進位
                                if (symbols.containsKey(varName)) {

                                    temp = symbols.get(varName);

                                    value = Parser.padLeftZero(Integer.toBinaryString(temp), 15);

                                } else {
                                   
                                    addressDec = symbols.size() + startAddress;

                                    
                                    if (addressDec >= 16384) {

                                        throw new IllegalStateException("Out of memory!Too many user defined symbols! Line " + lineNumber);

                                    }

                                    symbols.put(varName, addressDec);

                                    value = Parser.padLeftZero(Integer.toBinaryString(addressDec), 15);

                                }

                            } else {

                                throw new IllegalStateException("Illegal user-defined symbol! Line " + lineNumber);

                            }
                        }

                    }
                }

                instructions += "0" + value + "\n";

                pc++;

            }else if (pL.matcher(line).find()) {

                //忽略Ｌ指令
                continue;

            }else {
                //檢查是否為Ｃ指令

                flag1 = line.indexOf("=");
                flag2 = line.indexOf(";");
                dst = "";
                comp = "";
                jmp = "";

                //dest=comp;jump
                if (flag1 != -1 && flag2 != -1){

                    dst = line.substring(0,flag1);
                    comp = line.substring(flag1 + 1,flag2);
                    jmp = line.substring(flag2 + 1);

                //comp;jump
                }else if (flag1 == -1 && flag2 != -1){

                    comp = line.substring(0,flag2);
                    jmp = line.substring(flag2 + 1);

                //dest=comp
                }else if (flag1 != -1 && flag2 == -1){

                    dst = line.substring(0,flag1);
                    comp = line.substring(flag1 + 1);

                //dest
                }else {

                    dst = line;

                }

                if (dstMap.containsKey(dst) && (compMMap.containsKey(comp) || compAMap.containsKey(comp)) && jmpMap.containsKey(jmp)){

                    if (compAMap.containsKey(comp)){

                        a = "0";
                        comp = compAMap.get(comp);

                    }else {

                        a = "1";
                        comp = compMMap.get(comp);

                    }

                    instructions += "111" + a + comp + dstMap.get(dst) + jmpMap.get(jmp) + "\n";

                }else{

                    throw new IllegalStateException("Wrong instruction format!Line " + lineNumber);

                }

            }

        }

        return instructions;
    }

    /**
     * 將.asm存檔為.hack
     * @param dir
     */
    public static void translation(String dir){

        File fIn = new File(dir);

        //如果不是.asm停止動作
        if (!Parser.isAsm(fIn)){
            throw new IllegalArgumentException("Wrong file format! Only .asm is accepted!");
        }

        try {
            Scanner scan = new Scanner(fIn);
            String preprocessed = "";

            while (scan.hasNextLine()){

                String line = scan.nextLine();

                line = Parser.noSpaces(Parser.noComments(line));

                if (line.length() > 0){
                    preprocessed += line + "\n";
                }

            }
            preprocessed = preprocessed.trim();

            String result = asmToHack(preprocessed);

            String fileName = fIn.getName().substring(0,fIn.getName().indexOf("."));

            PrintWriter p = new PrintWriter(new File(fIn.getParentFile().getAbsolutePath() + "/" + fileName + ".hack"));

            p.print(result);

            p.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        if (args.length == 0){

            System.out.println("Usage: Assembler filename");
            return;

        }

        translation(args[0]);
        
    }

}
