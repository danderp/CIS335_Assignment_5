import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.System.exit;

public class siclink {
    static class externSymbol {
        String CTRLSEC;
        String symbol_name;
        int addr;
        int len;
        externSymbol() {
            CTRLSEC = "";
            symbol_name = "";
            addr = 0;
            len = 0;
        }
        externSymbol(String ctrl, String sym, int address, int length) {
            CTRLSEC = ctrl;
            symbol_name = sym;
            addr = address;
            len = length;
        }
        void printContents() {
            System.out.printf("Control Section: %s\nName: %s\nAddress: %s\nLength: %s\n", CTRLSEC, symbol_name, Integer.toHexString(addr).toUpperCase(), Integer.toHexString(len).toUpperCase());
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        int argc = args.length;
        if (argc < 1) {
            System.out.println("Usage: <PROGADDR> <fileA> <fileB> <fileC> ...");
            exit(0);
        }
        //init program address from cmdline arg
        int PROGADDR = Integer.parseInt(args[0], 16);
        int CSADDR = PROGADDR;
        int EXECADDR = PROGADDR;
        //init estab and mem
        //the estab will be an array of OBJECTs this time so i dont need 1000 parallel arrays (god)
        ArrayList<externSymbol> ESTAB = new ArrayList<>();
        char[] MEM = new char[1024*2];
        Arrays.fill(MEM, 'x');

        //pass 1
        //loop through for each program file
        for (int i=1; i<argc; i++) {
            String curr_cs = args[i];
            File file = new File(curr_cs+".txt");
            Scanner scanner = new Scanner(file);
            String file_line = "";
            String control_section = "";
            String name = "";
            int address = 0;
            int length = 0;
            //get h line
            file_line = scanner.nextLine();
            control_section = file_line.substring(1,7).trim();
            System.out.println(control_section);
            length = Integer.parseInt(file_line.substring(file_line.length()-6, file_line.length()), 16);
            System.out.println(length);

            while (scanner.hasNextLine()) {
                while (file_line.charAt(0) != 'E') {
                    file_line = scanner.nextLine();
                    if (file_line.charAt(0) == 'D') {
                        int current_object = 1;
                        while (current_object < file_line.length()) {
                            name = file_line.substring(current_object, current_object+6).trim();
                            for (int j=0; j< ESTAB.size(); j++) {
                                String estab_symbol = ESTAB.get(j).symbol_name;
                                if (estab_symbol.compareTo(name) == 0) {
                                    System.out.printf("Error: Duplicate symbol (%s) found", name);
                                }
                            }
                            current_object += 6;
                            address = Integer.parseInt(file_line.substring(current_object, current_object+6), 16);
                            address += CSADDR;
                            current_object += 6;
                            //init estab element based on the defined section name address and length
                            externSymbol estab = new externSymbol(control_section, name, address, length);
                            ESTAB.add(estab);
                        }
                    }
                }
            }
            CSADDR += length;
        }

        //pass 2
        for (int i=1; i<argc; i++) {
            String curr_cs = args[i];
            File file = new File(curr_cs+".txt");
            Scanner scanner = new Scanner(file);
            String file_line = "";
            String control_section = "";
            String name = "";
            int address = 0;
            int length = 0;
            //get h line
            file_line = scanner.nextLine();
            length = Integer.parseInt(file_line.substring(file_line.length()-6, file_line.length()), 16);
            System.out.println(length);

            while (scanner.hasNextLine()) {
                while (file_line.charAt(0) != 'E') {
                   file_line = scanner.nextLine();
                   if (file_line.charAt(0) != 'T') {
                       int object_code = Integer.parseInt("1");
                   } else if (file_line.charAt(0) != 'M') {

                   }
                }
            }
            CSADDR += length;
        }
        for (int i=0; i< ESTAB.size(); i++) {
            ESTAB.get(i).printContents();
        }
    }
}