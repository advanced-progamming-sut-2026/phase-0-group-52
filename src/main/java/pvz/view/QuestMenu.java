package pvz.view;

import pvz.model.App;

import java.util.Scanner;

public class QuestMenu implements AppMenu {

    @Override
    public void check(Scanner scanner) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        if (line.equals("menu show current")) {
            System.out.println("Current menu: " + App.getInstance().getCurrentmenu());
        } else if (line.startsWith("menu enter ")) {
            String name = line.substring("menu enter ".length()).trim();
            try {
                MenuType target = MenuType.fromName(name);
                if (target == null) throw new IllegalArgumentException();
                App.getInstance().setCurrentmenu(target);
                if (target.toMenu() != null) App.getInstance().setCurrentMenu(target.toMenu());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Unknown menu: " + name);
            }
        } else {
            invalidCommand();
        }
    }
}
