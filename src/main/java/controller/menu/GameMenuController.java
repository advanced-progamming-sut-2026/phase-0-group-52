package controller.menu;

import controller.Navigation;
import model.App;
import model.Game;
import model.GameField;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.Plant;
import model.entities.plants.PlantData;
import model.entities.plants.PlantFactory;
import model.entities.plants.PlantTag;
import model.entities.plants.Plants;
import model.entities.plants.types.PeaPod;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;
import model.level.ConveyorBeltLevel;
import model.level.PlantWhatYouGet;
import view.GameMenu;
import view.MenuType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameMenuController {

    private static final Pattern PLANT_CMD =
            Pattern.compile("^plant\\s+plant\\s+-t\\s+(.+?)\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern PLUCK_CMD =
            Pattern.compile("^pluck\\s+plant\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern FEED_CMD =
            Pattern.compile("^feed\\s+plant\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern TILE_CMD =
            Pattern.compile("^show\\s+tile\\s+status\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern SPAWN_CMD =
            Pattern.compile("^cheat\\s+spawn-zombie\\s+-t\\s+(\\S+)\\s+-l\\s*\\(?\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)?$");
    private static final Pattern COLLECT_LOC =
            Pattern.compile("^collect\\s+sun\\s+-l\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");

    private final App app;
    private final GameMenu view;
    private model.GameLoop loop;
    private Game loopGame;

    public GameMenuController(App app) {
        this.app = app;
        this.view = new GameMenu();
    }

    private model.GameLoop gameLoop(Game game) {
        if (loop == null || loopGame != game) {
            loop = new model.GameLoop(game);
            loopGame = game;
        }
        return loop;
    }

    public GameMenu getView() { return view; }

    public void handleCommand(String[] parts) {
        if (parts == null || parts.length == 0) {
            view.showError("Invalid command.");
            return;
        }
        String command = String.join(" ", parts).trim();
        switch (parts[0]) {
            case "menu":
                handleMenu(parts);
                break;
            case "plant":
                handlePlant(command);
                break;
            case "pluck":
                handlePluck(command);
                break;
            case "feed":
                handleFeed(command);
                break;
            case "cheat":
                handleCheat(command, parts);
                break;
            case "show":
                handleShow(command, parts);
                break;
            case "zombies":
                if (parts.length >= 2 && parts[1].equals("info")) {
                    Game game = requireGame();
                    if (game != null) view.showZombiesInfo(game.getZombies());
                } else {
                    view.showError("Usage: zombies info");
                }
                break;
            case "start":
                handleStartWaves(command);
                break;
            case "tick":
                handleTick(parts);
                break;
            case "advance":
                handleAdvanceTime(parts);
                break;
            case "collect":
                handleCollect(parts);
                break;
            default:
                view.showError("Unknown command: " + parts[0]);
        }
    }

    private void handleTick(String[] parts) {
        int n = 1;
        if (parts.length >= 2) {
            try { n = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
        }
        advanceTicks(n);
    }

    private void handleAdvanceTime(String[] parts) {
        int n = 1;
        for (int i = 1; i + 1 < parts.length; i++)
            if (parts[i].equals("-t")) {
                try { n = Integer.parseInt(parts[i + 1]); } catch (NumberFormatException ignored) {}
            }
        advanceTicks(n);
    }

    private void advanceTicks(int n) {
        Game game = requireGame();
        if (game == null) return;
        if (game.isGameOver()) { view.showError("The level is already over."); return; }
        model.GameLoop gl = gameLoop(game);
        for (int i = 0; i < n && !game.isGameOver(); i++) {
            String result = gl.step(game);
            if (result != null) {
                System.out.println(result);
                onLevelEnd(game);
                return;
            }
        }
        System.out.println("Tick " + game.getCurrentTick() + " | Sun: " + game.getSunAmount()
                + " | Zombies: " + game.getZombies().size()
                + " | Wave: " + (game.getCurrentWaveIndex() + 1) + "/" + game.getWaves().size());
    }

    private void recordZombiesMet(model.User user, Game game) {
        for (String alias : game.getStats().getKilledZombies()) {
            model.entities.zombies.ZombieRecord record =
                    model.entities.zombies.ZombieData.byAlias(alias);
            if (record != null && user.markZombieSeen(alias)) {
                user.getNewsList().addNews(
                        "A new zombie joined your almanac: " + record.getName() + "!");
            }
        }
    }

    private void onLevelEnd(Game game) {
        model.User u = app.getCurrentuser();
        if (game.isWon() && u != null) {
            System.out.println("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            u.setCoins(u.getCoins() + 500);
            recordZombiesMet(u, game);
            int score = game.getStats().getZombiesKilled() * 10 + game.getSunAmount();
            if (score > u.getMaxPoint()) u.setMaxPoint(score);
            advanceProgress(u, game);
            System.out.println("Reward: 500 coins, score " + score + ". Use 'menu enter chapter_menu' to continue.");
            int clearedLevel = game.getLevel() != null ? game.getLevel().getLevelnumber() : u.getLastLevel();
            if (clearedLevel >= u.getLastLevel()) {
                if (clearedLevel >= model.ChapterType.LEVELS_PER_CHAPTER) {
                    int nextChapter = Math.min(model.ChapterType.values().length,
                            Math.max(1, u.getLastChapter()) + 1);
                    u.setLastChapter(nextChapter);
                    u.setLastLevel(1);
                    u.getNewsList().addNews("Chapter cleared! A new world is open.");
                } else {
                    u.setLastLevel(clearedLevel + 1);
                    u.getNewsList().addNews("New level unlocked: level " + (clearedLevel + 1)
                            + "! A tougher wave awaits.");
                }
            }
        } else {
            System.out.println("Use 'menu enter chapter_menu' to try again.");
        }
        if (u != null) awardMeowPoints(game, u);
        new controller.QuestService().onLevelEnd(game, game.isWon());
        app.getPlantSelection().clear();
        app.getBoostedSelection().clear();
        app.setImitatedPlant(null);
        app.setAwaitingImitate(false);
        app.setPendingSpecial(null);
        app.getLockedPlants().clear();
        System.out.println("Your plant deck was reset. Pick a new deck in choose_plant_menu for the next level.");
    }

    private void awardMeowPoints(Game game, model.User u) {
        model.mechanics.MeowPointTracker meow = new model.mechanics.MeowPointTracker();
        model.GameStats s = game.getStats();
        java.util.Map<Integer, Integer> perTick = new java.util.HashMap<Integer, Integer>();
        for (int t : s.getKillTicks()) {
            Integer c = perTick.get(t);
            perTick.put(t, c == null ? 1 : c + 1);
        }
        for (int cnt : perTick.values())
            if (cnt > 1) meow.onSimultaneousKills(cnt);
        int fast = s.killsWithinTicksOfFirstWave(300);
        for (int i = 0; i < fast; i++) meow.onFastKill();
        if (game.isWon()) {
            if (s.getPlantsLost() == 0) meow.onPerfectDefense();
            if (s.getFirstWaveTick() >= 0 && game.getCurrentTick() - s.getFirstWaveTick() <= 600)
                meow.onWaveClearedQuickly();
        }
        int earned = meow.getPoints();
        meow.applyTo(u);
        System.out.println("Meow points earned this level: " + earned
                + " (best Meow Points: " + u.getMostMeowPoint() + ").");
    }

    private void advanceProgress(model.User u, Game game) {
        int levelsPerChapter = 4;
        int chapterNum = 1;
        if (game.getField() != null && game.getField().getChapter() != null)
            chapterNum = game.getField().getChapter().number();
        int levelNum = app.getSelectedLevel();
        int completed = (chapterNum - 1) * levelsPerChapter + levelNum;
        int curChapter = Math.max(1, u.getLastChapter());
        int curLevel = Math.max(1, u.getLastLevel());
        int currentPassed = (curChapter - 1) * levelsPerChapter + (curLevel - 1);
        if (completed > currentPassed) {
            u.setLastChapter(completed / levelsPerChapter + 1);
            u.setLastLevel(completed % levelsPerChapter + 1);
            System.out.println("Progress saved: chapter " + u.getLastChapter()
                    + ", level " + u.getLastLevel() + " unlocked.");
        }
    }

    private void handleCollect(String[] parts) {
        Game game = requireGame();
        if (game == null) return;
        if (parts.length < 2 || !parts[1].equals("sun")) {
            view.showError("Usage: collect sun  |  collect sun -l (<x>, <y>)");
            return;
        }
        Matcher m = COLLECT_LOC.matcher(String.join(" ", parts));
        if (m.matches()) {
            collectAt(game, Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            return;
        }
        int total = 0, count = 0;
        for (int i = game.getSuns().size() - 1; i >= 0; i--) {
            model.entities.Sun s = game.getSuns().get(i);
            if (!s.isFalling()) {
                total += s.getAmount();
                count++;
                game.getSuns().remove(i);
            }
        }
        if (count == 0) {
            System.out.println("No sun on the ground to collect yet.");
        } else {
            game.setSunAmount(game.getSunAmount() + total);
            game.getStats().addSunCollected(total);
            System.out.println("Collected " + count + " sun (+" + total + "). Total sun: " + game.getSunAmount() + ".");
        }
    }

    private void collectAt(Game game, int x, int y) {
        int col = x - 1, row = y - 1;
        for (int i = game.getSuns().size() - 1; i >= 0; i--) {
            model.entities.Sun s = game.getSuns().get(i);
            if (s.getCol() == col && s.getRow() == row) {
                if (s.isFalling() && s.getType() == model.entities.SunType.RADIOACTIVE) {
                    game.getSuns().remove(i);
                    detonateRadioactive(game, col, row);
                    return;
                }
                game.getSuns().remove(i);
                game.setSunAmount(game.getSunAmount() + s.getAmount());
                game.getStats().addSunCollected(s.getAmount());
                System.out.println("Collected sun (+" + s.getAmount() + "). Total sun: " + game.getSunAmount() + ".");
                return;
            }
        }
        System.out.println("No sun at (" + x + ", " + y + ").");
    }

    private void detonateRadioactive(Game game, int col, int row) {
        for (Zombie z : model.entities.plants.PlantCombat.zombiesInArea(game, col, row, 2))
            z.takeDamage(150);
        model.entities.plants.PlantCombat.removeDeadZombies(game);
        if (game.getField() != null)
            for (Plant p : new ArrayList<Plant>(game.getPlants()))
                if (Math.abs(p.getCol() - col) <= 1 && Math.abs(p.getRow() - row) <= 1)
                    p.takeDamage(80);
        System.out.println("Radioactive sun exploded at (" + (col + 1) + ", " + (row + 1) + ")!");
    }

    private void handleMenu(String[] parts) {
        if (parts.length >= 3 && parts[1].equals("show") && parts[2].equals("current")) {
            System.out.println("Current menu: " + app.getCurrentmenu());
            return;
        }
        if (parts.length >= 3 && parts[1].equals("enter")) {
            String navError = Navigation.enter(app, parts[2]);
            if (navError != null) view.showError(navError);
            return;
        }
        view.showError("Usage: menu show current  |  menu enter <menu_name>");
    }

    private void handlePlant(String command) {
        Game game = requireGame();
        if (game == null) return;
        Matcher m = PLANT_CMD.matcher(command);
        if (!m.matches()) {
            view.showError("Usage: plant plant -t <type> -l (<x>, <y>)");return;}
        Plants requested = findPlantType(m.group(1));if (requested == null) {
            view.showError("Unknown plant: " + m.group(1));return;}
        Plants type = requested;
        if (requested == Plants.IMITATER) {if (app.getImitatedPlant() == null) {
            view.showError("The Imitater has nothing to copy. Pick a plant to imitate in choose_plant_menu.");return;}
            type = app.getImitatedPlant();}
        if (game.getLevel() != null && !game.getLevel().isPlantAllowed(type)) {
            view.showError(type.getName() + " is not available in this level.");return;}
        if (!game.getChosenPlants().isEmpty()
                && !game.getChosenPlants().contains(requested) && !game.getChosenPlants().contains(type)) {
            view.showError(type.getName() + " was not in your chosen plants for this level.");return;}
        int x = Integer.parseInt(m.group(2));int y = Integer.parseInt(m.group(3));
        Cell cell = cellAt(game, x, y);
        if (cell == null) return;if (type == Plants.PEA_POD) {
            for (Plant p : cell.getPlants()) {if (p instanceof PeaPod) {
                    handlePeaPodHead(game, (PeaPod) p, x, y);return;}}}
        String plantError = plantError(cell, type);
        if (plantError != null) {view.showError(plantError);return;}
        boolean conveyor = game.getLevel() instanceof ConveyorBeltLevel;
        int plantLevel = app.getCurrentuser() != null ? app.getCurrentuser().getPlantLevel(type) : 1;
        int cost = PlantData.effectiveCost(type, plantLevel);
        if (conveyor && !((ConveyorBeltLevel) game.getLevel()).hasOnBelt(type)) {
            view.showError(type.getName() + " is not on the conveyor belt.");return;}
        if (!conveyor && !game.isCooldownsRemoved() && game.isOnCooldown(type)) {
            view.showError(type.getName() + " is recharging; ready in "
                    + String.format("%.1f", game.getRemainingCooldown(type)) + "s.");return;}
        if (!conveyor && game.getSunAmount() < cost) {
            view.showError("Not enough sun. Need " + cost
                    + ", have " + game.getSunAmount() + ".");return;}
        if (conveyor) ((ConveyorBeltLevel) game.getLevel()).takeFromBelt(type);
        else game.spendSun(cost);
        Plant plant = PlantFactory.create(type, new Vec2(x - 1, y - 1));
        PlantData.applyUpgrades(plant, plantLevel);
        cell.getPlants().add(plant);
        game.getPlants().add(plant);
        game.getStats().recordPlantPlanted(type, x - 1, y - 1);
        finishPlanting(game, plant, type, plantLevel, conveyor, x, y);
    }

    private void finishPlanting(Game game, Plant plant, Plants type, int plantLevel,
            boolean conveyor, int x, int y) {
        if (app.getCurrentuser() != null) {
            app.getCurrentuser().getPlants().addXp(type, 1);
        }
        if (!conveyor && !game.isCooldownsRemoved())
            game.startCooldown(type, PlantData.effectiveRecharge(type, plantLevel));
        plant.onPlanted(game);
        view.showPlanted(type.getName(), x, y);
        if (app.getCurrentuser() != null && app.getCurrentuser().getStoredBoosts().remove(type)){
            plant.boost();
            plant.onPlantFood(game);
            System.out.println(type.getName() + " was boosted by your greenhouse harvest!");}}

    private void handlePeaPodHead(Game game, PeaPod pod, int x, int y) {
        Plants type = Plants.PEA_POD;
        if (pod.getHeads() >= PeaPod.MAX_HEADS) {
            view.showError("Pea Pod at (" + x + ", " + y + ") already has "
                    + PeaPod.MAX_HEADS + " heads.");
            return;
        }
        if (!game.isCooldownsRemoved() && game.isOnCooldown(type)) {
            view.showError(type.getName() + " is recharging; ready in "
                    + String.format("%.1f", game.getRemainingCooldown(type)) + "s.");
            return;
        }
        if (game.getSunAmount() < type.getCost()) {
            view.showError("Not enough sun. Need " + type.getCost()
                    + ", have " + game.getSunAmount() + ".");
            return;
        }
        game.spendSun(type.getCost());
        pod.addHead();
        if (!game.isCooldownsRemoved()) game.startCooldown(type);
        System.out.println("Pea Pod at (" + x + ", " + y + ") now has "
                + pod.getHeads() + " heads.");
    }

    private String plantError(Cell cell, Plants type) {
        CellType cellType = cell.getType();

        if (type == Plants.HOT_POTATO)
            return cellType == CellType.FROZEN
                    ? null : "Hot Potato can only be planted on a frozen tile.";
        if (type == Plants.GRAVE_BUSTER)
            return cellType == CellType.TOMBSTONE
                    ? null : "Grave Buster can only be planted on a tombstone.";

        if (!cell.getPlants().isEmpty()) {
            boolean hasLilyPad = false;
            for (Plant p : cell.getPlants())
                if (p.getType() == Plants.LILY_PAD) hasLilyPad = true;

            if (hasLilyPad && type != Plants.LILY_PAD && !type.getTags().contains(PlantTag.WATER)) {
                if (cell.getPlants().size() >= 2)
                    return "This Lily Pad already holds a plant.";
                return null;
            }
            return "This tile already has a plant.";
        }

        if (cellType == CellType.WATER) {
            boolean isWaterPlant = type.getTags().contains(PlantTag.WATER);
            return isWaterPlant ? null
                    : "Only water plants can be planted on water. Place a Lily Pad first.";
        }
        if (!cellType.isPlantable())
            return "Cannot plant on this tile (" + cellType + ").";
        return null;
    }

    private Plants findPlantType(String input) {
        String normalized = input.trim().replace(' ', '_').replace('-', '_');
        for (Plants p : Plants.values()) {
            if (p.getName().equalsIgnoreCase(input.trim())
                    || p.name().equalsIgnoreCase(normalized))
                return p;
        }
        return null;
    }

    private void handlePluck(String command) {
        Game game = requireGame();
        if (game == null) return;
        Matcher m = PLUCK_CMD.matcher(command);
        if (!m.matches()) {
            view.showError("Usage: pluck plant -l (<x>, <y>)");
            return;
        }
        int x = Integer.parseInt(m.group(1));
        int y = Integer.parseInt(m.group(2));
        Cell cell = cellAt(game, x, y);
        if (cell == null) return;
        if (cell.getPlants().isEmpty()) {
            view.showError("No plant at (" + x + ", " + y + ").");
            return;
        }
        Plant top = cell.getPlants().remove(cell.getPlants().size() - 1);
        game.getPlants().remove(top);
        view.showPlucked(top.getType().getName(), x, y);
    }

    private void handleFeed(String command) {
        Game game = requireGame();
        if (game == null) return;
        Matcher m = FEED_CMD.matcher(command);
        if (!m.matches()) {
            view.showError("Usage: feed plant -l (<x>, <y>)");
            return;
        }
        if (game.getPlantFoodCount() <= 0) {
            view.showError("You have no plant food.");
            return;
        }
        int x = Integer.parseInt(m.group(1));
        int y = Integer.parseInt(m.group(2));
        Cell cell = cellAt(game, x, y);
        if (cell == null) return;
        if (cell.getPlants().isEmpty()) {
            view.showError("No plant at (" + x + ", " + y + ").");
            return;
        }
        Plant top = cell.getPlants().get(cell.getPlants().size() - 1);
        game.setPlantFoodCount(game.getPlantFoodCount() - 1);
        top.onPlantFood(game);
        view.showFed(top.getType().getName(), x, y, game.getPlantFoodCount());
    }

    private void handleCheat(String command, String[] parts) {
        Game game = requireGame();
        if (game == null) return;
        if (parts.length < 2) {
            view.showError("Usage: cheat remove-cooldown  |  cheat add-plant-food  |" +
                    "  cheat spawn-zombie -t <type> -l <x, y>");
            return;
        }
        switch (parts[1]) {
            case "remove-cooldown":
                game.clearAllCooldowns();
                game.setCooldownsRemoved(true);
                view.showCooldownsRemoved();
                break;
            case "add-plant-food":
                game.setPlantFoodCount(game.getPlantFoodCount() + 1);
                view.showPlantFoodAdded(game.getPlantFoodCount());
                break;
            case "spawn-zombie":
                handleSpawnZombie(game, command);
                break;
            case "add":
                int suns = 100;
                for (int i = 2; i + 1 < parts.length; i++)
                    if (parts[i].equals("-n")) {
                        try { suns = Integer.parseInt(parts[i + 1]); } catch (NumberFormatException ignored) {}
                    }
                game.setSunAmount(game.getSunAmount() + suns);
                System.out.println("Added " + suns + " sun. Total sun: " + game.getSunAmount() + ".");
                break;
            case "sun":
                int amount = 100;
                if (parts.length >= 3) {
                    try { amount = Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) {}
                }
                game.setSunAmount(game.getSunAmount() + amount);
                System.out.println("Added " + amount + " sun. Total sun: " + game.getSunAmount() + ".");
                break;
            default:
                view.showError("Unknown cheat: " + parts[1]);
        }
    }

    private void handleSpawnZombie(Game game, String command) {
        Matcher m = SPAWN_CMD.matcher(command);
        if (!m.matches()) {
            view.showError("Usage: cheat spawn-zombie -t <zombie-type> -l <x, y>");
            return;
        }
        Zombies data = findZombieType(m.group(1));
        if (data == null) {
            view.showError("Unknown zombie: " + m.group(1));
            return;
        }
        int x = Integer.parseInt(m.group(2));
        int y = Integer.parseInt(m.group(3));
        GameField field = game.getField();
        if (field == null || !field.inBounds(x - 1, y - 1)) {
            view.showError("Location out of bounds: (" + x + ", " + y + ").");
            return;
        }
        Zombie zombie = ZombieFactory.create(data, y - 1, new Vec2(x - 1, y - 1),
                field.getChapter(), null);
        game.getZombies().add(zombie);
        view.showZombieSpawned(zombie.getClass().getSimpleName(), x, y);
    }

    private Zombies findZombieType(String input) {
        String normalized = input.trim().replace('-', '_').replace(' ', '_');
        for (Zombies z : Zombies.values()) {
            if (z.name().equalsIgnoreCase(normalized)
                    || z.name().equalsIgnoreCase("ZOMBIE_" + normalized)
                    || z.getName().equalsIgnoreCase(input.trim())
                    || z.getName().equalsIgnoreCase("Zombie" + input.trim()))
                return z;
        }
        return null;
    }

    private void handleShow(String command, String[] parts) {
        Game game = requireGame();
        if (game == null) return;
        if (parts.length >= 2 && parts[1].equals("map")) {
            view.showMap(game);
            return;
        }
        if (parts.length >= 2 && parts[1].equals("sun")) {
            System.out.println("Sun: " + game.getSunAmount()
                    + " | Plant food: " + game.getPlantFoodCount() + "/" + Game.MAX_PLANT_FOOD);
            return;
        }
        if (parts.length >= 3 && parts[1].equals("plants") && parts[2].equals("status")) {
            List<Plants> types = game.getChosenPlants().isEmpty()
                    ? Arrays.asList(Plants.values())
                    : new ArrayList<Plants>(game.getChosenPlants());
            view.showPlantsStatus(types, game);
            return;
        }
        Matcher m = TILE_CMD.matcher(command);
        if (m.matches()) {
            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            Cell cell = cellAt(game, x, y);
            if (cell == null) return;
            List<Zombie> zombiesHere = new ArrayList<Zombie>();
            for (Zombie z : game.getZombies())
                if (z.getRow() == y - 1 && z.getCol() == x - 1) zombiesHere.add(z);
            view.showTileStatus(cell, zombiesHere, x, y);
            return;
        }
        view.showError("Usage: show map  |  show sun  |  show plants status  |  show tile status -l (<x>, <y>)");
    }

    private void handleStartWaves(String command) {
        if (!command.equals("start zombie waves")) {
            view.showError("Usage: start zombie waves");
            return;
        }
        Game game = requireGame();
        if (game == null) return;
        if (!(game.getLevel() instanceof PlantWhatYouGet)) {
            view.showError("This command is only available in Plant What You Get levels.");
            return;
        }
        PlantWhatYouGet level = (PlantWhatYouGet) game.getLevel();
        if (level.isWavesStarted()) {
            view.showError("Zombie waves have already started.");
            return;
        }
        level.startWaves(game.getCurrentTick());

        if (!game.getWaves().isEmpty()) {
            game.setCurrentWaveIndex(0);
            model.Wave first = game.getWaves().get(0);
            game.getZombies().addAll(first.getZombies());
            System.out.println("Zombie waves started! Wave 1 incoming! "
                    + first.getZombies().size() + " zombie(s).");
        } else {
            System.out.println("Zombie waves started!");
        }
    }

    private Game requireGame() {
        Game game = app.getGame();
        if (game == null) view.showError("No game in progress.");
        return game;
    }

    private Cell cellAt(Game game, int x, int y) {
        GameField field = game.getField();
        if (field == null || !field.inBounds(x - 1, y - 1)) {
            view.showError("Location out of bounds: (" + x + ", " + y + ").");
            return null;
        }
        return field.getCell(x - 1, y - 1);
    }
}
