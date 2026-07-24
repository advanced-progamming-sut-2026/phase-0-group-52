package view;

import controller.menu.MainMenuController;
import model.App;

import java.util.Scanner;

public class MainMenu implements AppMenu {

    private MainMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new MainMenuController(App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line.split("\\s+"));
    }

    public void showLoggedOut() {
        System.out.println("Logged out successfully.");
    }

    public void showLeaderboard(java.util.List<model.Leaderboard.Entry> entries,
                                String column, boolean ascending) {
        if (entries.isEmpty()) {
            System.out.println("Leaderboard: no registered players yet.");
            return;
        }
        System.out.println("Leaderboard (sorted by " + column + (ascending ? " asc" : " desc") + "):");
        System.out.printf("%-4s %-15s %-18s %-10s %-13s %-13s %s%n",
                "#", "Username", "Progress", "Minigames", "DailyQuests", "OtherQuests", "HighScore");
        int rank = 1;
        for (model.Leaderboard.Entry entry : entries) {
            System.out.printf("%-4d %-15s %-18s %-10d %-13d %-13d %d%n",
                    rank++, entry.getUser().getUsername(), entry.getProgressText(),
                    entry.getUser().getMiniGamesPlayed(), entry.getUser().getQuestDailyNum(),
                    entry.getUser().getQuestNonDailyNum(), entry.getUser().getMaxPoint());
        }
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }
}
