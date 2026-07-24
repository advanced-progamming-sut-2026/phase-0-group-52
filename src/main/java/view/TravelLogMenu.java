package view;

import controller.menu.TravelLogMenuController;
import minigame.Minigame;
import minigame.MinigameType;
import model.App;
import model.enums.commands.TravelLogCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class TravelLogMenu implements AppMenu {

    private final TravelLogMenuController controller = new TravelLogMenuController();

    @Override
    public void check(Scanner scanner) {
        String line = scanner.nextLine().trim();

        if (TravelLogCommands.TRAVEL_LOG_PAGE.matches(line)) {
            Matcher m = TravelLogCommands.TRAVEL_LOG_PAGE.getMatcher(line);
            controller.showPage(m.group("page"));

        } else if (TravelLogCommands.PLAY_MINIGAME.matches(line)) {
            Matcher m = TravelLogCommands.PLAY_MINIGAME.getMatcher(line);
            MinigameType type = Minigame.findType(m.group("name"));
            if (type == null) {
                System.out.println("Unknown minigame: " + m.group("name"));
            } else {
                new Minigame(type, Integer.parseInt(m.group("level")))
                        .start(App.getInstance().getLoggedInUser(), scanner);
            }

        } else if (TravelLogCommands.SHOW_QUESTS.matches(line)) {
            controller.showCurrentPage();

        } else if (TravelLogCommands.CURRENT_MENU.matches(line)) {
            controller.showCurrentMenu();

        } else if (TravelLogCommands.EXIT_MENU.matches(line)) {
            controller.exitMenu();

        } else {
            invalidCommand();
        }
    }
}
