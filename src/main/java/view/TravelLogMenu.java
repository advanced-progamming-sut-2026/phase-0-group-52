package view;

import controller.TravelLogMenuController;
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
