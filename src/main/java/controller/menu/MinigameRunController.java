package controller.menu;

import minigame.MinigameType;
import model.App;
import model.Game;
import model.LevelBuilder;
import model.Result;
import model.entities.BowlingNut;
import model.entities.Vase;
import model.entities.plants.Plants;
import model.entities.zombies.Zombies;
import model.enums.MenuType;
import model.level.IZombieLevel;
import model.level.ScoreAttackLevel;
import model.level.VasebreakerLevel;
import model.level.WallNutBowlingLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinigameRunController extends LevelController {

    private final App app;
    private final MinigameMenuController menu;

    private MinigameType kind;
    private Plants held;
    private boolean scored;

    public MinigameRunController(App app) {
        super(app);
        this.app = app;
        this.menu = new MinigameMenuController(app);
    }

    public MinigameType kind() {
        return kind;
    }

    @Override
    public Result start() {
        MinigameType wanted = MinigameType.byKey(app.getPendingMinigame());
        if (wanted == null || !wanted.isLawnBased()) {
            return new Result(false, "No minigame selected.", null);
        }
        kind = wanted;
        held = null;
        scored = false;
        resetRun();
        Game game = LevelBuilder.buildMinigame(app, kind, levelNumber());
        if (game == null) {
            return new Result(false, "That minigame could not start.", null);
        }
        app.setGame(game);
        app.setCurrentmenu(MenuType.MINIGAME_MENU);
        return new Result(true, kind.getDisplayName() + " started.", kind);
    }

    @Override
    public int levelNumber() {
        return Math.max(1, app.getSelectedLevel());
    }

    @Override
    public Result restart() {
        app.setGame(null);
        app.setPendingMinigame(kind == null ? null : kind.name());
        return start();
    }

    @Override
    public Result leave() {
        bankScore();
        app.setGame(null);
        app.setPendingMinigame(null);
        resetRun();
        app.setCurrentmenu(MenuType.MAIN_MENU);
        return new Result(true, "Left the minigame.", null);
    }

    @Override
    public void tick(float delta) {
        super.tick(delta);
        if (outcome() != null) {
            bankScore();
        }
    }

    public int score() {
        Game game = game();
        if (game == null) {
            return 0;
        }
        if (game.getLevel() instanceof ScoreAttackLevel) {
            return ((ScoreAttackLevel) game.getLevel()).getScore();
        }
        return game.getStats().getZombiesKilled() * ScoreAttackLevel.POINTS_PER_KILL;
    }

    public int best() {
        return menu.bestScore(kind);
    }

    private void bankScore() {
        if (scored || kind == null) {
            return;
        }
        scored = true;
        menu.recordScore(kind, score());
    }

    public boolean isBowling() {
        return level(WallNutBowlingLevel.class) != null;
    }

    public boolean isVasebreaker() {
        return level(VasebreakerLevel.class) != null;
    }

    public boolean isIZombie() {
        return level(IZombieLevel.class) != null;
    }

    public boolean isScoreAttack() {
        return level(ScoreAttackLevel.class) != null;
    }

    public List<Plants> bowlingBelt() {
        WallNutBowlingLevel bowling = level(WallNutBowlingLevel.class);
        return bowling == null ? new ArrayList<Plants>()
                : new ArrayList<Plants>(bowling.getBelt());
    }

    public List<BowlingNut> nuts() {
        WallNutBowlingLevel bowling = level(WallNutBowlingLevel.class);
        return bowling == null ? new ArrayList<BowlingNut>() : bowling.getNuts();
    }

    public Result roll(int column, int row) {
        WallNutBowlingLevel bowling = level(WallNutBowlingLevel.class);
        if (bowling == null) {
            return new Result(false, "Not a bowling level.", null);
        }
        if (!bowling.isBowlingColumn(column)) {
            return new Result(false, "Bowl from the first three columns.", null);
        }
        if (!bowling.roll(game(), column, row)) {
            return new Result(false, "No wall-nut ready yet.", null);
        }
        return new Result(true, "Rolling!", null);
    }

    public List<Vase> vases() {
        VasebreakerLevel vasebreaker = level(VasebreakerLevel.class);
        return vasebreaker == null ? new ArrayList<Vase>() : vasebreaker.getVases();
    }

    public int vasesLeft() {
        VasebreakerLevel vasebreaker = level(VasebreakerLevel.class);
        return vasebreaker == null ? 0 : vasebreaker.unbroken();
    }

    public Plants held() {
        return held;
    }

    public Result smash(int column, int row) {
        VasebreakerLevel vasebreaker = level(VasebreakerLevel.class);
        if (vasebreaker == null) {
            return new Result(false, "Not a vase level.", null);
        }
        if (held != null && vasebreaker.place(game(), held, column, row)) {
            Plants placed = held;
            held = null;
            return new Result(true, placed.getName() + " planted.", placed);
        }
        if (vasebreaker.vaseAt(column, row) == null) {
            return new Result(false, held == null
                    ? "No vase there." : "You cannot plant there.", null);
        }
        Plants prize = vasebreaker.smash(game(), column, row);
        if (prize == null) {
            return new Result(true, "Smash!", null);
        }
        held = prize;
        return new Result(true, "You found a " + prize.getName() + " - place it!", prize);
    }

    public Map<Zombies, Integer> zombieShop() {
        IZombieLevel izombie = level(IZombieLevel.class);
        return izombie == null
                ? new java.util.LinkedHashMap<Zombies, Integer>() : izombie.getShop();
    }

    public double zombieRecharge(Zombies type) {
        IZombieLevel izombie = level(IZombieLevel.class);
        return izombie == null ? 0d : izombie.rechargeLeft(type);
    }

    public float zombieRechargeFraction(Zombies type) {
        double left = zombieRecharge(type);
        return left <= 0d ? 0f
                : (float) Math.min(1d, left / IZombieLevel.RECHARGE_SECONDS);
    }

    public int brainsLeft() {
        IZombieLevel izombie = level(IZombieLevel.class);
        return izombie == null ? 0 : izombie.getBrains().size();
    }

    public Result buy(Zombies type, int row) {
        IZombieLevel izombie = level(IZombieLevel.class);
        if (izombie == null) {
            return new Result(false, "Not an I, Zombie level.", null);
        }
        if (game().getSunAmount() < izombie.priceOf(type)) {
            return new Result(false, "Not enough sun.", null);
        }
        if (izombie.isRecharging(type)) {
            return new Result(false, type.name() + " is still recharging.", null);
        }
        if (!izombie.buy(game(), type, row)) {
            return new Result(false, "That zombie cannot go there.", null);
        }
        return new Result(true, "Sent in a zombie.", type);
    }

    public int scoreTarget() {
        ScoreAttackLevel score = level(ScoreAttackLevel.class);
        return score == null ? 0 : score.getTargetScore();
    }

    public float scoreProgress() {
        ScoreAttackLevel score = level(ScoreAttackLevel.class);
        return score == null ? 0f : score.progress();
    }

    private <T> T level(Class<T> wanted) {
        Game game = game();
        if (game == null || game.getLevel() == null
                || !wanted.isInstance(game.getLevel())) {
            return null;
        }
        return wanted.cast(game.getLevel());
    }
}
