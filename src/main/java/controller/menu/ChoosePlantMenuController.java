package controller.menu;

import controller.Navigation;
import model.App;
import model.ChapterType;
import model.Game;
import model.LevelBuilder;
import model.User;
import model.entities.plants.Plants;
import model.enums.MenuType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChoosePlantMenuController {

    public static final int FIRST_LEVEL_SLOTS = 5;
    public static final int OTHER_LEVEL_SLOTS = 8;

    private int maxSlots() {
        return app.getSelectedLevel() <= 1 ? FIRST_LEVEL_SLOTS : OTHER_LEVEL_SLOTS;
    }

    private static final Pattern CHOOSE = Pattern.compile("^(?:choose|add\\s+plant)\\s+-t\\s+(.+)$");
    private static final Pattern REMOVE = Pattern.compile("^remove(?:\\s+plant)?\\s+-t\\s+(.+)$");
    private static final Pattern BOOST = Pattern.compile("^boost(?:\\s+plant)?\\s+-t\\s+(.+)$");

    private final App app;

    public ChoosePlantMenuController(App app) {
        this.app = app;
    }

    public void handleCommand(String line) {
        String command = line.trim();
        String[] parts = command.split("\\s+");
        if (parts[0].equals("menu")) {
            handleMenu(parts);
            return;
        }
        Matcher m;
        if (command.equals("show plants") || command.equals("show all plants")
                || command.equals("show available plants")) {
            showAvailable();
        } else if (command.equals("show selection")) {
            showSelection();
        } else if ((m = CHOOSE.matcher(command)).matches()) {
            choose(m.group(1));
        } else if ((m = REMOVE.matcher(command)).matches()) {
            remove(m.group(1));
        } else if ((m = BOOST.matcher(command)).matches()) {
            boost(m.group(1));
        } else if (command.equals("clear selection")) {
            app.getPlantSelection().clear();
            app.getBoostedSelection().clear();
            app.setImitatedPlant(null);
            app.setAwaitingImitate(false);
            System.out.println("Selection cleared.");
        } else if (command.equals("start") || command.startsWith("start level")
                || command.startsWith("start -l")) {
            startLevel(parts);
        } else {
            System.out.println("invalid command");
        }
    }

    private void startLevel(String[] parts) {
        if (app.getCurrentuser() == null) { System.out.println("Error: No user is logged in."); return; }
        ChapterType chapter = app.getSelectedChapter();
        if (chapter == null) {
            System.out.println("Error: Enter a chapter first (chapter_menu).");
            return;
        }
        if (app.getPlantSelection().isEmpty()) {
            System.out.println("Error: Choose at least one plant before starting.");
            return;
        }
        int levelNumber = app.getSelectedLevel();
        for (int i = 1; i + 1 < parts.length; i++)
            if (parts[i].equals("-l")) {
                try { levelNumber = Integer.parseInt(parts[i + 1]); app.setSelectedLevel(levelNumber); }
                catch (NumberFormatException ignored) {}
            }
        String special = app.getPendingSpecial();
        Game game;
        if (special != null) {
            game = LevelBuilder.buildSpecial(app, chapter, levelNumber, special);
            app.setPendingSpecial(null);
            if (game == null) { System.out.println("Error: Could not start the special level '" + special + "'.");
                return; }
        } else {game = LevelBuilder.build(app, chapter, levelNumber);}
        for (Plants p : app.getBoostedSelection())
            app.getCurrentuser().getStoredBoosts().add(p);
        app.getBoostedSelection().clear();
        game.setApp(app);
        app.setGame(game);
        app.setCurrentmenu(MenuType.GAME_MENU);
        app.setCurrentmenu(MenuType.GAME_MENU);
        if (special != null) {
            System.out.println("Special level '" + special + "' started in " + chapter + " (level "
                    + levelNumber + ") with " + app.getPlantSelection().size()
                    + " plant(s). Starting sun: " + game.getSunAmount() + ".");
        } else {
            System.out.println("Level started in " + chapter + " (level " + levelNumber
                    + ") with " + app.getPlantSelection().size() + " plant(s). Starting sun: "
                    + game.getSunAmount() + ".");
        }
    }

    private void showAvailable() {
        System.out.println("Available plants (choose up to " + maxSlots() + "):");
        List<Plants> selection = app.getPlantSelection();
        for (Plants p : Plants.values()) {
            String mark = app.getLockedPlants().contains(p) ? " [LOCKED]"
                    : (selection.contains(p) ? " [chosen]" : "");
            System.out.println("  " + p.getName() + " | cost: " + p.getCost() + " sun" + mark);
        }
    }

    private void showSelection() {
        List<Plants> selection = app.getPlantSelection();
        if (selection.isEmpty()) {
            System.out.println("No plants chosen yet. (use: choose -t <plant>)");
            return;
        }
        System.out.println("Chosen plants (" + selection.size() + "/" + maxSlots() + "):");
        for (Plants p : selection) {
            boolean boosted = app.getBoostedSelection().contains(p);
            System.out.println("  " + p.getName() + (boosted ? " [boosted]" : ""));
        }
    }

    private void choose(String name) {
        Plants type = findPlant(name);
        if (type == null) { System.out.println("Error: Unknown plant: " + name); return; }
        if (app.isAwaitingImitate()) {
            if (type == Plants.IMITATER) {
                System.out.println("Error: The Imitater cannot copy itself. Choose a different plant.");
                return;
            }
            app.setImitatedPlant(type);
            app.setAwaitingImitate(false);
            System.out.println("The Imitater will copy " + type.getName() + " this level.");
            return;
        }
        if (app.getLockedPlants().contains(type)) {
            System.out.println("Error: " + type.getName() + " is locked this level.");
            return;
        }
        List<Plants> selection = app.getPlantSelection();
        if (selection.contains(type)) { System.out.println(type.getName() + " is already chosen."); return; }
        if (selection.size() >= maxSlots()) {
            System.out.println("Error: All " + maxSlots() + " slots are full. Remove one first.");
            return;
        }
        selection.add(type);
        System.out.println("Chose " + type.getName() + " (" + selection.size() + "/" + maxSlots() + ").");
        if (type == Plants.IMITATER) {
            app.setAwaitingImitate(true);
            System.out.println("Now choose the plant for the Imitater to copy: 'add plant -t <plant>'.");
        }
    }

    private void remove(String name) {
        Plants type = findPlant(name);
        if (type == null) { System.out.println("Error: Unknown plant: " + name); return; }
        if (app.getPlantSelection().remove(type)) {
            app.getBoostedSelection().remove(type);
            System.out.println("Removed " + type.getName() + " from the selection.");
        } else {
            System.out.println(type.getName() + " is not in the selection.");
        }
    }

    private void boost(String name) {
        User user = app.getCurrentuser();
        if (user == null) { System.out.println("Error: No user is logged in."); return; }
        Plants type = findPlant(name);
        if (type == null) { System.out.println("Error: Unknown plant: " + name); return; }
        if (!app.getPlantSelection().contains(type)) {
            System.out.println("Error: Choose " + type.getName() + " before boosting it.");
            return;
        }
        if (!user.getStoredBoosts().contains(type)) {
            System.out.println("Error: No stored greenhouse boost for " + type.getName() + ".");
            return;
        }
        user.getStoredBoosts().remove(type);
        app.getBoostedSelection().add(type);
        System.out.println(type.getName() + " will start the level boosted (plant food applied on plant).");
    }

    private Plants findPlant(String input) {
        String normalized = input.trim().replace(' ', '_').replace('-', '_');
        for (Plants p : Plants.values())
            if (p.getName().equalsIgnoreCase(input.trim()) || p.name().equalsIgnoreCase(normalized))
                return p;
        return null;
    }

    private void handleMenu(String[] parts) {
        if (parts.length >= 3 && parts[1].equals("show") && parts[2].equals("current")) {
            System.out.println("Current menu: " + app.getCurrentmenu());
            return;
        }
        if (parts.length >= 3 && parts[1].equals("enter")) {
            String navError = Navigation.enter(app, parts[2]);
            if (navError != null) System.out.println("Error: " + navError);
            return;
        }
        System.out.println("Error: Usage: menu show current  |  menu enter <menu_name>");
    }
}
