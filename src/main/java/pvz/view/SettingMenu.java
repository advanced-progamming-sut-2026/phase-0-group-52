package pvz.view;

import pvz.controller.menu.SettingMenuController;
import pvz.model.App;

import java.util.Scanner;

public class SettingMenu implements AppMenu {

    private SettingMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new SettingMenuController(App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line.split("\\s+"));
    }

    public void showDifficultyChanged(int level) {
        System.out.println("Difficulty changed to " + level + ".");
    }

    public void showInvalidDifficulty() {
        System.out.println("Error: Difficulty level must be between 1 and 5.");
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }
}
