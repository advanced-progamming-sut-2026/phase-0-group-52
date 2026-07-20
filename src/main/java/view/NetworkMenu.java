package view;

import controller.Navigation;
import model.App;

import java.util.Scanner;

public class NetworkMenu implements AppMenu {

    @Override
    public void check(Scanner scanner) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        if (line.equals("menu show current")) {
            System.out.println("Current menu: " + App.getInstance().getCurrentmenu());
        } else if (line.startsWith("menu enter ")) {
            String err = Navigation.enter(App.getInstance(), line.substring("menu enter ".length()).trim());
            if (err != null) System.out.println("Error: " + err);
        } else {
            System.out.println("The network menu will be available in a later phase.");
        }
    }
}
