import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.System.exit;

public class siclink {
    class externSymbol {
        String CTRLSEC;
        String symbol_name;
        int addr;
        String len;
        externSymbol() {
            CTRLSEC = "";
            symbol_name = "";
            addr = 0;
            len = "";
        }
        externSymbol(String ctrl, String sym, int address, String length) {
            CTRLSEC = ctrl;
            symbol_name = sym;
            addr = address;
            len = length;
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        int argc = args.length;
        if (argc < 1) {
            System.out.println("Usage: <PROGADDR> <fileA> <fileB> <fileC> ...");
            exit(0);
        }
        //init program address from cmdline arg
        int PROGADDR = Integer.parseInt(args[0]);
        //init estab and mem
        //the estab will be an array of OBJECTs this time so i dont need 1000 parallel arrays (god)
        ArrayList<externSymbol> ESTAB = new ArrayList<>();
        char[] MEM = new char[1024];
        String curr_cs = args[1];
        File file = new File(curr_cs+".txt");
        Scanner scanner = new Scanner(file);
        String file_line = "";
        int line_count = 0;
        while (scanner.hasNextLine()) {
            file_line = scanner.nextLine();
            System.out.println(file_line);
            line_count++;
        }
        //pass 1
        while (scanner.hasNextLine()) {
            while (scanner.nextLine().charAt(0) != 'E') {

            }
        }
    }
}