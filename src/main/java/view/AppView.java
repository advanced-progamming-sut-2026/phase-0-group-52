package view;

import model.App;
import java.util.Scanner;

public class AppView {
    public static void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            App.getInstance().getCurrentMenu().checkCommand(scanner);
        }
    }
}
