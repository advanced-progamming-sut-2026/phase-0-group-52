package view;

import controller.menu.CollectionMenuController;
import model.App;

import java.util.Scanner;

public class CollectionMenu implements AppMenu {

    private CollectionMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new CollectionMenuController(App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line);
    }
}
