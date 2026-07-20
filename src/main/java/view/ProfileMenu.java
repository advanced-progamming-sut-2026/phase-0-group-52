package view;

import controller.menu.ProfileMenuController;
import model.App;

import java.util.Scanner;

public class ProfileMenu implements AppMenu {

    private ProfileMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new ProfileMenuController(App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line);
    }
}
