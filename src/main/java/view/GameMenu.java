package view;

import controller.GameMenuController;
import model.enums.commands.GameCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class GameMenu implements AppMenu {
    private final GameMenuController controller = new GameMenuController();

    @Override
    public void check(Scanner scanner) {
        String line = scanner.nextLine().trim();

        if (GameCommands.ADVANCE_TIME.matches(line)) {
            Matcher m = GameCommands.ADVANCE_TIME.getMatcher(line);
            controller.advanceTime(Integer.parseInt(m.group("count")));

        } else if (GameCommands.COLLECT_SUN.matches(line)) {
            Matcher m = GameCommands.COLLECT_SUN.getMatcher(line);
            controller.collectSun(Integer.parseInt(m.group("x")), Integer.parseInt(m.group("y")));

        } else if (GameCommands.SHOW_SUN.matches(line)) {
            controller.showSunAmount();

        } else if (GameCommands.CHEAT_ADD_SUN.matches(line)) {
            Matcher m = GameCommands.CHEAT_ADD_SUN.getMatcher(line);
            controller.cheatAddSun(Integer.parseInt(m.group("count")));

        } else if (GameCommands.PLANT_PLANT.matches(line)) {
            Matcher m = GameCommands.PLANT_PLANT.getMatcher(line);
            controller.plantPlant(m.group("type"),
                Integer.parseInt(m.group("x")), Integer.parseInt(m.group("y")));

        } else if (GameCommands.PLUCK_PLANT.matches(line)) {
            Matcher m = GameCommands.PLUCK_PLANT.getMatcher(line);
            controller.pluckPlant(Integer.parseInt(m.group("x")), Integer.parseInt(m.group("y")));

        } else if (GameCommands.FEED_PLANT.matches(line)) {
            Matcher m = GameCommands.FEED_PLANT.getMatcher(line);
            controller.feedPlant(Integer.parseInt(m.group("x")), Integer.parseInt(m.group("y")));

        } else if (GameCommands.CHEAT_REMOVE_COOLDOWN.matches(line)) {
            controller.cheatRemoveCooldown();

        } else if (GameCommands.CHEAT_ADD_PLANT_FOOD.matches(line)) {
            controller.cheatAddPlantFood();

        } else if (GameCommands.SHOW_MAP.matches(line)) {
            controller.showMap();

        } else if (GameCommands.SHOW_PLANTS_STATUS.matches(line)) {
            controller.showPlantsStatus();

        } else if (GameCommands.SHOW_TILE_STATUS.matches(line)) {
            Matcher m = GameCommands.SHOW_TILE_STATUS.getMatcher(line);
            controller.showTileStatus(Integer.parseInt(m.group("x")), Integer.parseInt(m.group("y")));

        } else if (GameCommands.RELEASE_NUKE.matches(line)) {
            controller.releaseNuke();

        } else if (GameCommands.CURRENT_MENU.matches(line)) {
            controller.showCurrentMenu();

        } else if (GameCommands.EXIT_MENU.matches(line)) {
            controller.exitMenu();

        } else {
            invalidCommand();
        }
    }
}
