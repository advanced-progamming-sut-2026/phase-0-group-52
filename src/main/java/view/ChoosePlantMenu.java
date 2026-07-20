package view;

import controller.menu.ChoosePlantMenuController;
import model.App;

import java.util.Scanner;

public class ChoosePlantMenu implements AppMenu {

    private ChoosePlantMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new ChoosePlantMenuController(App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line);
    }
}
