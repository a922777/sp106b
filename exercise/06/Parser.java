import java.io.File;

public class Parser {

    /**
     * 刪除註解//
     * @param strIn
     * @return
     */
    public static String noComments(String strIn){

        int position = strIn.indexOf("//");

        if (position != -1){

            strIn = strIn.substring(0, position);

        }

        return strIn;
    }

    /**
     * 刪除空白
     * @param strIn
     * @return
     */
    public static String noSpaces(String strIn){
        String result = "";

        if (strIn.length() != 0){

            String[] segs = strIn.split(" ");

            for (String s: segs){
                result += s;
            }
        }

        return result;
    }

    /**
     * 回傳到.asm的資料夾
     * @param fileIn
     * @return
     */
    public static boolean isAsm(File fileIn){

        String filename = fileIn.getName();
        int position = filename.lastIndexOf(".");

        if (position != -1) {

            String ext = filename.substring(position);

            if (ext.toLowerCase().equals(".asm")) {
                return true;
            }
        }

        return false;
    }

    /**
     *補０
     * @param strIn
     * @param len
     * @return
     */
    public static String padLeftZero(String strIn, int len){

        for (int i = strIn.length(); i < len; i++){
            strIn = "0" + strIn;
        }

        return strIn;
    }

}
