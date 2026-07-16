package pvz.view;

import pvz.controller.menu.GreenhouseMenuController;
import pvz.model.App;

import java.util.Scanner;

public class GreenhouseMenu implements AppMenu {

    private GreenhouseMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new GreenhouseMenuController(App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line);
    }
}
