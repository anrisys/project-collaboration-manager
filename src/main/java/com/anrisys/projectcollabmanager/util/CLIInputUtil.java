package com.anrisys.projectcollabmanager.util;

import java.util.Scanner;

public class CLIInputUtil {
    private static final Scanner scanner;

    static {
        scanner = new Scanner(System.in);
    }

    public static int requestIntInput() {
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public static String requestStringInput() {
        System.out.print("> ");
        return scanner.nextLine().trim();
    }

    public static boolean requestBooleanInput() {
        while (true) {
            System.out.print("(yes/no) > ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes") || input.equals("y")) return true;
            if (input.equals("no") || input.equals("n")) return false;
            System.out.println("Please enter yes or no.");
        }
    }
}


