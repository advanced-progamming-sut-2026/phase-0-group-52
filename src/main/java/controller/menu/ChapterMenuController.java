package controller.menu;

import controller.Navigation;
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
            if (parts.length >= 2 && parts[1].equals("special")) handleStartSpecial(parts);
            else handleStartLevel(parts);
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
            case "seed-wallet":
            case "seed-packets":
                if (app.getCurrentuser() == null) { view.showError("No user is logged in."); break; }
                view.showSeedWallet(app.getCurrentuser().getSeedPacket());
                break;
            case "cheat":
                handleCheat(parts);
                break;
            case "leaderboard":
                handleLeaderboard(parts);
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
        String navError = Navigation.enter(app, parts[2]);
        if (navError != null) view.showError(navError);
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
        int level = 1;
        for (int i = 5; i + 1 < parts.length; i++)
            if (parts[i].equals("-l")) {
                try { level = Integer.parseInt(parts[i + 1]); } catch (NumberFormatException ignored) {}
            }
        app.setSelectedLevel(level);
        app.setSelectedChapter(chapter);
        view.showEnteredChapter(chapter.name());
        System.out.println("Level " + level + " selected. Now pick your deck: 'menu enter choose_plant_menu'"
                + " (up to " + (level <= 1 ? ChoosePlantMenuController.FIRST_LEVEL_SLOTS
                : ChoosePlantMenuController.OTHER_LEVEL_SLOTS) + " plants), then 'start' there.");
    }

    private void handleStartLevel(String[] parts) {
        if (app.getCurrentuser() == null) {
            view.showError("No user is logged in.");
            return;}
        ChapterType chapter = currentChapter;
        int levelNumber = app.getSelectedLevel();
        for (int i = 1; i + 1 < parts.length; i++) {
            if (parts[i].equals("-c")) {
                try { chapter = ChapterType.valueOf(parts[i + 1].toUpperCase()); }
                catch (IllegalArgumentException e) { view.showError("Invalid chapter: " + parts[i + 1]); return; }
            } else if (parts[i].equals("-l")) {
                try { levelNumber = Integer.parseInt(parts[i + 1]); } catch (NumberFormatException ignored) {}}
        }
        app.setSelectedLevel(levelNumber);
        if (chapter == null) {
            view.showError("Enter a chapter first: menu enter chapter -c <chapter>");
            return;}
        if (app.getPlantSelection().isEmpty()) {
            view.showError("Pick your plant deck first: 'menu enter choose_plant_menu', "
                    + "choose plants, then 'start'.");
            return;}
        if (app.getPendingSpecial() != null) {
            String sp = app.getPendingSpecial();
            Game sgame = LevelBuilder.buildSpecial(app, chapter, levelNumber, sp);
            app.setPendingSpecial(null);
            if (sgame == null) { view.showError("Unknown special level: " + sp + "."); return; }
            for (Plants p : app.getBoostedSelection())
                app.getCurrentuser().getStoredBoosts().add(p);
            app.getBoostedSelection().clear();
            sgame.setApp(app);
            app.setGame(sgame);
            app.setCurrentmenu(MenuType.GAME_MENU);
            app.setCurrentMenu(Menu.GameMenu);
            System.out.println("Special level '" + sp + "' started in " + chapter + " (level "
                    + levelNumber + "). Starting sun: " + sgame.getSunAmount() + ".");
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
                + " | show sun | show map | show plants status | feed | cheat | zombies info | menu enter <menu>");
    }

    private void handleLeaderboard(String[] parts) {
        String column = "score";
        boolean ascending = false;
        for (int i = 2; i < parts.length; i++) {
            if (parts[i].equals("-s") && i + 1 < parts.length) column = parts[i + 1].toLowerCase();
            else if (parts[i].equals("-a")) ascending = true;
            else if (parts[i].equals("-d")) ascending = false;
        }
        java.util.List<model.Leaderboard.Entry> entries = new model.Leaderboard().getEntries(column, ascending);
        System.out.println("== Leaderboard (by " + column + (ascending ? ", asc" : ", desc") + ") ==");
        System.out.printf("%-4s %-16s %-7s %-18s %-10s %-7s%n",
                "#", "Username", "Score", "Progress", "Minigames", "Quests");
        int rank = 1;
        for (model.Leaderboard.Entry e : entries) {
            User u = e.getUser();
            System.out.printf("%-4d %-16s %-7d %-18s %-10d %-7d%n", rank++, u.getUsername(),
                    u.getMaxPoint(), e.getProgressText(), u.getMiniGamesPlayed(), u.getQuestNonDailyNum());
        }
        if (entries.isEmpty()) System.out.println("(no players yet)");
        System.out.println("Sort: menu leaderboard -s <score|level|minigames|daily|quests> [-a|-d]");
    }

    private void handleStartSpecial(String[] parts) {
        if (app.getCurrentuser() == null) {
            view.showError("No user is logged in.");
            return;}
        ChapterType chapter = currentChapter;
        int levelNumber = app.getSelectedLevel();
        String special = null;
        for (int i = 2; i + 1 < parts.length; i++) {
            if (parts[i].equals("-t")) special = parts[i + 1];
            else if (parts[i].equals("-c")) {
                try { chapter = ChapterType.valueOf(parts[i + 1].toUpperCase()); }
                catch (IllegalArgumentException e) { view.showError("Invalid chapter: " + parts[i + 1]); return; }
            } else if (parts[i].equals("-l")) {
                try { levelNumber = Integer.parseInt(parts[i + 1]); } catch (NumberFormatException ignored) {}}}
        if (chapter == null) {
            view.showError("Enter a chapter first: menu enter chapter -c <chapter>");return;}
        if (special == null) {
            view.showError("Usage: start special -t <type>. Types: " + LevelBuilder.specialTypes());return;}
        String key = special.toLowerCase().replace("-", "").replace("_", "");
        boolean providesPlants = key.equals("conveyor") || key.equals("conveyorbelt")
                || key.equals("plantwhatyouget") || key.equals("pwyg");
        if (!providesPlants) {
            app.setSelectedChapter(chapter);
            app.setSelectedLevel(levelNumber);
            app.getPlantSelection().clear();
            app.getBoostedSelection().clear();
            app.getLockedPlants().clear();
            if (key.equals("lockedplants") || key.equals("locked")) {
                app.getLockedPlants().addAll(java.util.Arrays.asList(
                        model.entities.plants.Plants.CHERRY_BOMB, model.entities.plants.Plants.JALAPENO,
                        model.entities.plants.Plants.SQUASH, model.entities.plants.Plants.REPEATER,
                        model.entities.plants.Plants.WINTER_MELON));
            }
            app.setPendingSpecial(special);
            Navigation.go(app, MenuType.CHOOSE_PLANT_MENU);
            System.out.println("Special level '" + special + "': pick your plant deck in choose_plant_menu"
                    + (app.getLockedPlants().isEmpty() ? "" : " (some plants are LOCKED — use 'show plants')")
                    + ", then 'start'.");return;}
        Game game = LevelBuilder.buildSpecial(app, chapter, levelNumber, special);
        if (game == null) {
            view.showError("Unknown special level: " + special + ". Types: " + LevelBuilder.specialTypes());return;}
        game.setApp(app);
        app.setGame(game);
        app.setCurrentmenu(MenuType.GAME_MENU);
        app.setCurrentMenu(Menu.GameMenu);
        System.out.println("Special level '" + special + "' started in " + chapter
                + ". Starting sun: " + game.getSunAmount() + ".");
        System.out.println("Commands: plant plant -t <type> -l (x, y) | collect sun | tick [n]"
                + " | show map | start zombie waves | menu enter <menu>");}

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
