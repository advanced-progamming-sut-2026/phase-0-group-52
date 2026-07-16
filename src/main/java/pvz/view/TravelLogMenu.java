package pvz.view;

import pvz.controller.menu.TravelLogMenuController;
import pvz.model.App;

import java.util.Scanner;

public class TravelLogMenu implements AppMenu {

    private TravelLogMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new TravelLogMenuController(App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line, scanner);
    }
}
