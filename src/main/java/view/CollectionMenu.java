package view;

import model.App;

import java.util.Scanner;

public class CollectionMenu implements AppMenu {

    @Override
    public void check(Scanner scanner) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        if (line.equals("menu show current")) {
            System.out.println("Current menu: " + App.getInstance().getCurrentmenu());
        } else if (line.startsWith("menu enter ")) {
            String target = line.substring("menu enter ".length()).trim();
            MenuType t = MenuType.fromName(target);
            if (t == null) { System.out.println("Error: Unknown menu: " + target); return; }
            App.getInstance().setCurrentmenu(t);
            if (t.toMenu() != null) App.getInstance().setCurrentMenu(t.toMenu());
        } else {
            invalidCommand();
        }
    }
}