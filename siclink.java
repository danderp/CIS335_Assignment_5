import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.System.exit;
import static java.lang.System.in;

public class siclink {
    public static class externSymbol {
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
    public static class instruction {
        String address;
        int internal_val;
        int starting_loc;
        int ending_loc;
        instruction() {
            address = "";
            internal_val = 0;
            starting_loc = 0;
            ending_loc = 0;
        }
        instruction(String addr, int intern, int start, int end) {
            address = addr;
            internal_val = intern;
            starting_loc = start;
            ending_loc = end;
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        int argc = args.length;
        if (argc < 1) {
            System.out.println("Usage: <fileA> <fileB> <fileC> ...");
            exit(0);
        }
        //init program address from cmdline arg
        Scanner input = new Scanner(System.in);
        System.out.print("Enter a Hex value for PROGADDR: ");
        String prog = input.nextLine();
        int PROGADDR = Integer.parseInt(prog, 16);
        int CSADDR = PROGADDR;
        int EXECADDR = PROGADDR;
        //init estab and mem
        //the estab will be an array of OBJECTs this time so i dont need 1000 parallel arrays (god)
        ArrayList<externSymbol> ESTAB = new ArrayList<>();
        char[] MEM = new char[1024*64];
        Arrays.fill(MEM, '.');

        //pass 1
        //loop through for each program file
        for (int i=0; i<argc; i++) {
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
            length = Integer.parseInt(file_line.substring(file_line.length()-6), 16);
            //enter control section into the ESTAB
            externSymbol hline = new externSymbol(control_section, control_section, CSADDR, length);
            ESTAB.add(hline);

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
//        for (int i=0; i< ESTAB.size(); i++) {
//            ESTAB.get(i).printContents();
//        }

        //pass 2
        CSADDR = PROGADDR;
        for (int i=0; i<argc; i++) {
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
            length = Integer.parseInt(file_line.substring(file_line.length()-6), 16);

            while (scanner.hasNextLine()) {
                while (file_line.charAt(0) != 'E') {
                   file_line = scanner.nextLine();
                   if (file_line.charAt(0) == 'T') {
                       //get size of text line in half-bytes
                       int text_size = Integer.parseInt(file_line.substring(7,9), 16);
                       //get starting location to store bytes
                       int starting_loc = Integer.parseInt(file_line.substring(1, 7), 16) + CSADDR;
                       file_line = file_line.substring(9);
                       //move every half-byte individually into the memory space
                       for (int j = 0; j<2*text_size; j++) {
                           MEM[(2*starting_loc)+j] = file_line.charAt(j);
                       }
                   } else if (file_line.charAt(0) == 'M') {
                       //will be assuming that a call for 5 half-byte modification means the instruction
                       //is of format 4 because i dont know why you would ever modify the first 5 hex vals of a format 3
                       int half_bytes = Integer.parseInt(file_line.substring(7,9), 16);
                       //get starting location to store bytes
                       int starting_loc = Integer.parseInt(file_line.substring(1, 7), 16) + CSADDR;
                       char operator = file_line.charAt(9);
                       name=file_line.substring(10);
                       if (isInTable(ESTAB, name) >= 0) {
                           int modification = ESTAB.get(isInTable(ESTAB, name)).addr;
                           //System.out.printf("%s\n", name);
                           StringBuilder instruction = new StringBuilder();
                           if (half_bytes == 5) {
                               for (int j=0; j<half_bytes; j++) {
                                   instruction.append(MEM[(2*starting_loc)+j+1]);
                               }
                           }
                           if (half_bytes == 6) {
                               for (int j=0; j<half_bytes; j++) {
                                   instruction.append(MEM[(2*starting_loc)+j]);
                               }
                           }
                           String modified = "";
                           if (operator == '+') {
                               modified = modifyInstruction(instruction, modification, 0, half_bytes);
                           }
                           if (operator == '-') {
                               modified = modifyInstruction(instruction, modification, 1, half_bytes);
                           }
                           instruction.delete(0, instruction.length());
                           if (half_bytes == 5) {
                               for (int j=0; j<half_bytes; j++) {
                                   MEM[(2*starting_loc)+j+1] = modified.charAt(j);
                               }
                           }
                           if (half_bytes == 6) {
                               for (int j=0; j<half_bytes; j++) {
                                   MEM[(2*starting_loc)+j] = modified.charAt(j);
                               }
                           }
                       } else {
                           System.out.printf("Error: Undefined external symbol (%s)\n", name);
                           exit(1);
                       }
                   }
                }
                if (file_line.length() > 1) {
                    EXECADDR = CSADDR + Integer.parseInt(file_line.substring(1, 7), 16);
                }
            }
            CSADDR += length;
        }
        printMemSpace(MEM, 2*(PROGADDR), 2*(CSADDR), EXECADDR);
    }
    public static int isInTable(ArrayList<externSymbol> arrayList, String name) {
        for (int j=0; j<arrayList.size(); j++) {
            if (arrayList.get(j).symbol_name.compareTo(name) == 0) {
                return j;
            }
        }
        return -1;
    }
    public static void printMemSpace(char[] mem, int start, int end, int EXECADDR) {
        try {
            PrintWriter file_writer = new PrintWriter("outfile.txt");
            file_writer.printf("%s  ", Integer.toHexString(start/2));
            for (int i=start; i<end; i++) {
                file_writer.printf("%s", mem[i]);

                if ((i+1) % 32 == 0) {
                    file_writer.printf("\n%s  ", Integer.toHexString((i/2)+1).toUpperCase());
                }
                else if ((i+1) % 8 == 0) {
                    file_writer.print("  ");
                }
            }
            file_writer.println();
            file_writer.printf("EXECADDR: %s", Integer.toHexString(EXECADDR).toUpperCase());
            file_writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String modifyInstruction(StringBuilder instruct, int operation, int operator, int size) {
        //0=+, 1=-
        int int_instruct = Integer.parseInt(instruct.toString(), 16);
        int sum = int_instruct+operation;
        int difference = int_instruct-operation;
        StringBuilder sum_hex = new StringBuilder(Integer.toHexString(sum).toUpperCase());
        StringBuilder diff_hex = new StringBuilder(Integer.toHexString(difference).toUpperCase());
        if (operator == 0) {
            if (sum < 0) {
                return padStringFront(sum_hex, "F", sum_hex.length(), size);
            }
            else {
                return padStringFront(sum_hex, "0", sum_hex.length(), size);
            }
        }
        if (operator == 1) {
            if (difference < 0) {
                return padStringFront(diff_hex, "F", diff_hex.length(), size);
            }
            else {
                return padStringFront(diff_hex, "0", diff_hex.length(), size);
            }
        }
        return "ERROR";
    }
    public static String padStringFront(StringBuilder string, String padding, int string_size, int padded_size) {
        String return_string = "";
        if (string_size < padded_size) {
            for (int i = 0; i<padded_size-string_size; i++) {
                string.insert(0, padding);
            }
        }
        return_string = string.toString();
        if (string_size > padded_size) {
            return_string = string.substring(string_size-padded_size);
        }
        return return_string;
        }
}