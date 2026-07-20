package controller.menu;

import model.App;
import model.User;
import model.entities.plants.Plants;
import view.MenuType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChoosePlantMenuController {

    public static final int MAX_SLOTS = 7;

    private static final Pattern CHOOSE = Pattern.compile("^choose\\s+-t\\s+(.+)$");
    private static final Pattern REMOVE = Pattern.compile("^remove\\s+-t\\s+(.+)$");
    private static final Pattern BOOST = Pattern.compile("^boost\\s+-t\\s+(.+)$");

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
        if (command.equals("show plants")) {
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
            System.out.println("Selection cleared.");
        } else {
            System.out.println("invalid command");
        }
    }

    private void showAvailable() {
        System.out.println("Available plants (choose up to " + MAX_SLOTS + "):");
        List<Plants> selection = app.getPlantSelection();
        for (Plants p : Plants.values()) {
            String mark = selection.contains(p) ? " [chosen]" : "";
            System.out.println("  " + p.getName() + " | cost: " + p.getCost() + " sun" + mark);
        }
    }

    private void showSelection() {
        List<Plants> selection = app.getPlantSelection();
        if (selection.isEmpty()) {
            System.out.println("No plants chosen yet. (use: choose -t <plant>)");
            return;
        }
        System.out.println("Chosen plants (" + selection.size() + "/" + MAX_SLOTS + "):");
        for (Plants p : selection) {
            boolean boosted = app.getBoostedSelection().contains(p);
            System.out.println("  " + p.getName() + (boosted ? " [boosted]" : ""));
        }
    }

    private void choose(String name) {
        Plants type = findPlant(name);
        if (type == null) { System.out.println("Error: Unknown plant: " + name); return; }
        List<Plants> selection = app.getPlantSelection();
        if (selection.contains(type)) { System.out.println(type.getName() + " is already chosen."); return; }
        if (selection.size() >= MAX_SLOTS) {
            System.out.println("Error: All " + MAX_SLOTS + " slots are full. Remove one first.");
            return;
        }
        selection.add(type);
        System.out.println("Chose " + type.getName() + " (" + selection.size() + "/" + MAX_SLOTS + ").");
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
            MenuType target = MenuType.fromName(parts[2]);
            if (target == null) { System.out.println("Error: Unknown menu: " + parts[2]); return; }
            app.setCurrentmenu(target);
            if (target.toMenu() != null) app.setCurrentMenu(target.toMenu());
            return;
        }
        System.out.println("Error: Usage: menu show current  |  menu enter <menu_name>");
    }
}
