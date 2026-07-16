package pvz.controller.menu;

import pvz.minigame.Minigame;
import pvz.minigame.MinigameType;
import pvz.model.App;
import pvz.model.quest.Quests;
import pvz.view.MenuType;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TravelLogMenuController {

    private static final Pattern PAGE_CMD = Pattern.compile("^travel\\s+log\\s+page\\s+(\\S+)$");
    private static final Pattern PLAY_CMD = Pattern.compile("^play\\s+(\\S+)\\s+([1-3])$");

    private final App app;

    public TravelLogMenuController(App app) {
        this.app = app;
    }

    public void handleCommand(String line, Scanner scanner) {
        String command = line.trim();
        String[] parts = command.split("\\s+");
        if (parts[0].equals("menu")) {
            handleMenu(parts);
            return;
        }
        Matcher m;
        if ((m = PAGE_CMD.matcher(command)).matches()) {
            showPage(m.group(1).toLowerCase());
            return;
        }
        if ((m = PLAY_CMD.matcher(command)).matches()) {
            play(m.group(1).toLowerCase(), Integer.parseInt(m.group(2)), scanner);
            return;
        }
        System.out.println("invalid command");
    }

    private void showPage(String page) {
        switch (page) {
            case "critical":
            case "high":
            case "medium":
            case "low":
                System.out.println("Quests on page " + page + ":");
                boolean any = false;
                for (Quests q : Quests.values()) {
                    if (q.getPriority().name().equalsIgnoreCase(page)) {
                        System.out.println("  " + q.name() + " | category: " + q.getCategory());
                        any = true;
                    }
                }
                if (!any) System.out.println("  (no quests on this page yet)");
                break;
            case "minigames":
                System.out.println("Minigames (each has 3 levels, use: play <name> <1-3>):");
                for (MinigameType type : MinigameType.values())
                    System.out.println("  " + type.name().toLowerCase());
                break;
            default:
                System.out.println("Error: Unknown page: " + page
                        + ". Pages: critical, high, medium, low, minigames");
        }
    }

    private void play(String name, int level, Scanner scanner) {
        MinigameType type = Minigame.findType(name);
        if (type == null) {
            System.out.println("Error: Unknown minigame: " + name);
            return;
        }
        new Minigame(type, level).start(app.getCurrentuser(), scanner);
    }

    private void handleMenu(String[] parts) {
        if (parts.length >= 3 && parts[1].equals("show") && parts[2].equals("current")) {
            System.out.println("Current menu: " + app.getCurrentmenu());
            return;
        }
        if (parts.length >= 3 && parts[1].equals("enter")) {
            MenuType target = MenuType.fromName(parts[2]);
            if (target == null) {
                System.out.println("Error: Unknown menu: " + parts[2]);
                return;
            }
            app.setCurrentmenu(target);
            if (target.toMenu() != null) app.setCurrentMenu(target.toMenu());
            return;
        }
        System.out.println("Error: Usage: menu show current  |  menu enter <menu_name>");
    }
}
