package controller.menu;

import model.App;
import model.ChapterType;
import model.Game;
import model.LevelBuilder;
import model.User;
import model.entities.plants.Plants;
import model.enums.Menu;
import view.ChapterMenu;
import view.MenuType;

public class ChapterMenuController {

    private final App app;
    private final ChapterMenu view;
    private ChapterType currentChapter;

    public ChapterMenuController(App app) {
        this.app = app;
        this.view = new ChapterMenu();
    }

    public void handleCommand(String[] parts) {
        if (parts.length == 0) return;
        if (parts[0].equals("start")) {
            handleStartLevel(parts);
            return;
        }
        if (!parts[0].equals("menu")) {
            view.showError("Unknown command: " + parts[0]);
            return;
        }
        if (parts.length < 2) {
            view.showError("Invalid command.");
            return;
        }
        switch (parts[1]) {
            case "show":
                if (parts.length >= 3 && parts[2].equals("current"))
                    System.out.println("Current menu: " + app.getCurrentmenu());
                else
                    view.showError("Usage: menu show current");
                break;
            case "enter":
                handleEnter(parts);
                break;
            case "coin-wallet":
                if (app.getCurrentuser() == null) { view.showError("No user is logged in."); break; }
                view.showCoinWallet(app.getCurrentuser().getCoins());
                break;
            case "gem-wallet":
                if (app.getCurrentuser() == null) { view.showError("No user is logged in."); break; }
                view.showGemWallet(app.getCurrentuser().getGems());
                break;
            case "cheat":
                handleCheat(parts);
                break;
            case "leaderboard":
                System.out.println("Leaderboard: (no records yet)");
                break;
            default:
                view.showError("Unknown command: " + parts[1]);
        }
    }

    private void handleEnter(String[] parts) {
        if (parts.length < 3) {
            view.showError("Usage: menu enter <menu_name>  |  menu enter chapter -c <chapterName>");
            return;
        }
        if (parts[2].equals("chapter")) {
            handleEnterChapter(parts);
            return;
        }
        try {
            MenuType target = MenuType.fromName(parts[2]);
            if (target == null) throw new IllegalArgumentException();
            app.setCurrentmenu(target);
            if (target.toMenu() != null) app.setCurrentMenu(target.toMenu());
        } catch (IllegalArgumentException e) {
            view.showError("Unknown menu: " + parts[2]);
        }
    }

    private void handleEnterChapter(String[] parts) {
        if (parts.length < 5 || !parts[3].equals("-c")) {
            view.showError("Usage: menu enter chapter -c <chapterName>");
            return;
        }
        ChapterType chapter;
        try {
            chapter = ChapterType.valueOf(parts[4].toUpperCase());
        } catch (IllegalArgumentException e) {
            view.showError("Invalid chapter: " + parts[4] +
                    ". Options: ANCIENT_EGYPT, FROSTBITE_CAVES, BIG_WAVE_BEACH, DARK_AGES");
            return;
        }
        currentChapter = chapter;
        view.showEnteredChapter(chapter.name());
        System.out.println("Use 'start level [-l <number>]' to begin, or first pick plants in choose_plant_menu.");
    }

    private void handleStartLevel(String[] parts) {
        if (app.getCurrentuser() == null) {
            view.showError("No user is logged in.");
            return;
        }
        ChapterType chapter = currentChapter;
        int levelNumber = 1;
        for (int i = 1; i + 1 < parts.length; i += 2) {
            if (parts[i].equals("-c")) {
                try { chapter = ChapterType.valueOf(parts[i + 1].toUpperCase()); }
                catch (IllegalArgumentException e) { view.showError("Invalid chapter: " + parts[i + 1]); return; }
            } else if (parts[i].equals("-l")) {
                try { levelNumber = Integer.parseInt(parts[i + 1]); } catch (NumberFormatException ignored) {}
            }
        }
        if (chapter == null) {
            view.showError("Enter a chapter first: menu enter chapter -c <chapter>");
            return;
        }
        Game game = LevelBuilder.build(app, chapter, levelNumber);
        for (Plants p : app.getBoostedSelection())
            app.getCurrentuser().getStoredBoosts().add(p);
        app.getBoostedSelection().clear();
        game.setApp(app);
        app.setGame(game);
        app.setCurrentmenu(MenuType.GAME_MENU);
        app.setCurrentMenu(Menu.GameMenu);
        System.out.println("Level started in " + chapter + " (level " + levelNumber
                + "). Starting sun: " + game.getSunAmount() + ".");
        System.out.println("Commands: plant plant -t <type> -l (x, y) | collect sun | tick [n]"
                + " | show map | show plants status | feed | cheat | zombies info | menu enter <menu>");
    }

    private void handleCheat(String[] parts) {
        if (app.getCurrentuser() == null) {
            view.showError("No user is logged in.");
            return;
        }
        if (parts.length < 5 || !parts[2].equals("add")) {
            view.showError("Usage: menu cheat add <n> <coin/diamond>");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            view.showError("Invalid amount: " + parts[3]);
            return;
        }
        if (amount < 0) {
            view.showError("Amount must be non-negative.");
            return;
        }
        User user = app.getCurrentuser();
        switch (parts[4].toLowerCase()) {
            case "coin":
                user.setCoins(user.getCoins() + amount);
                view.showCheatResult(amount, "coin", user.getCoins());
                break;
            case "diamond":
                user.setGems(user.getGems() + amount);
                view.showCheatResult(amount, "diamond", user.getGems());
                break;
            default:
                view.showError("Invalid type: " + parts[4] + ". Use 'coin' or 'diamond'.");
        }
    }
}
