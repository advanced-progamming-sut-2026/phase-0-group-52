package pvz.view;

import pvz.controller.menu.ChapterMenuController;
import pvz.model.App;

import java.util.Scanner;

public class ChapterMenu implements AppMenu {

    private ChapterMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new ChapterMenuController(App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line.split("\\s+"));
    }

    public void showEnteredChapter(String chapterName) {
        System.out.println("Entered chapter: " + chapterName);
    }

    public void showCoinWallet(int coins) {
        System.out.println("Coin Wallet: " + coins + " coins");
    }

    public void showGemWallet(int gems) {
        System.out.println("Gem Wallet: " + gems + " gems");
    }

    public void showCheatResult(int amount, String type, int newBalance) {
        System.out.println("Added " + amount + " " + type + "(s). New balance: " + newBalance);
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }
}
