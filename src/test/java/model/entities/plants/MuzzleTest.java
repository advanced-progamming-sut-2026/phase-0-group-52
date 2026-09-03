package model.entities.plants;

import model.ChapterType;
import model.Game;
import model.GameField;
import model.GameLoop;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.Projectile;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MuzzleTest {

    private Game lawn() {
        GameField field = new GameField(ChapterType.ANCIENT_EGYPT);
        Game game = new Game(null, null, null, field, 0, new ArrayList<Plant>(),
                new ArrayList<model.Wave>());
        clearTombs(game);
        return game;
    }

    private void clearTombs(Game game) {
        game.getTombstones().clear();
        for (int c = 0; c < game.getField().getCols(); c++) {
            for (int r = 0; r < game.getField().getRows(); r++) {
                Cell cell = game.getField().getCell(c, r);
                if (cell != null && cell.getType() == CellType.TOMBSTONE) {
                    cell.setType(CellType.NORMAL);
                }
            }
        }
    }

    private Plant plant(Game game, Plants type, int column, int row) {
        Plant made = PlantFactory.create(type, new Vec2(column, row));
        game.getField().getCell(column, row).getPlants().add(made);
        game.getPlants().add(made);
        made.onPlanted(game);
        return made;
    }

    private void fillRows(Game game, int column) {
        for (int row = 0; row < game.getField().getRows(); row++) {
            game.getZombies().add(ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, row, column,
                    ChapterType.ANCIENT_EGYPT));
        }
    }

    private void surround(Game game, int column) {
        fillRows(game, column);
        for (int row = 0; row < game.getField().getRows(); row++) {
            game.getZombies().add(ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, row, 0,
                    ChapterType.ANCIENT_EGYPT));
        }
    }

    private Set<String> portsFired(Game game, double seconds) {
        GameLoop loop = new GameLoop(game);
        Set<String> seen = new HashSet<String>();
        int ticks = (int) Math.round(seconds * Game.TICKS_PER_SECOND);
        for (int i = 0; i < ticks; i++) {
            loop.step(game);
            clearTombs(game);
            for (Projectile shot : game.getProjectiles()) {
                seen.add(shot.getPort());
            }
        }
        return seen;
    }

    @Test
    void everyMuzzleAPlantDeclaresActuallyFires() {
        for (Plants type : Plants.values()) {
            if (type.getCategory() != PlantsCategory.SHOOTER
                    || type == Plants.APPEASE_MINT || type == Plants.PEA_POD
                    || type == Plants.FIRE_PEASHOOTER) {
                continue;
            }
            Game game = lawn();
            Plant made = plant(game, type, 2, 2);
            surround(game, 6);
            List<Muzzle> ports = ((Shooter) made).ports();
            made.onPlantFood(game);
            Set<String> fired = portsFired(game, Plant.FED_SHOW + 1d);
            fired.addAll(portsFired(game, 20d));
            for (Muzzle muzzle : ports) {
                assertTrue(fired.contains(muzzle.getName()),
                        type.getName() + " never fired from its " + muzzle.getName()
                                + " muzzle, only " + fired);
            }
        }
    }

    @Test
    void threepeaterCoversItsThreeLanes() {
        Game game = lawn();
        Plant made = plant(game, Plants.THREEPEATER, 2, 2);
        fillRows(game, 6);
        Set<String> plain = portsFired(game, 8d);
        assertEquals(new HashSet<String>(java.util.Arrays.asList("up", "mid", "down")), plain,
                "Threepeater covers its three lanes and no more");
    }

    @Test
    void aPeaPodOnlyFiresFromTheHeadsItHasGrown() {
        Game game = lawn();
        Plant made = plant(game, Plants.PEA_POD, 2, 2);
        fillRows(game, 6);
        assertEquals(java.util.Collections.singleton("head1"), portsFired(game, 6d),
                "a one-headed Pea Pod should only use its first muzzle");

        Game grown = lawn();
        model.entities.plants.types.PeaPod pod =
                (model.entities.plants.types.PeaPod) plant(grown, Plants.PEA_POD, 2, 2);
        pod.addHead();
        pod.addHead();
        fillRows(grown, 6);
        assertEquals(3, portsFired(grown, 6d).size(),
                "a three-headed Pea Pod should use three muzzles");
    }

    @Test
    void aFedShooterFiresItsOwnPlantFoodArtWhereOneExists() {
        assertTrue(view.gui.ShotArt.rig(Plants.REPEATER, Projectile.Kind.PEA, "fed")
                        .contains("GIANTPEA"),
                "a fed Repeater should fire the giant pea named in plants.json");
        assertTrue(view.gui.ShotArt.rig(Plants.STARFRUIT, Projectile.Kind.STAR, "fed")
                        .contains("PLANTFOOD"),
                "a fed Starfruit should fire its plant-food star");
        assertEquals(view.gui.ShotArt.rig(Plants.PEASHOOTER, Projectile.Kind.PEA, ""),
                view.gui.ShotArt.rig(Plants.PEASHOOTER, Projectile.Kind.PEA, "fed"),
                "a plant with no plant-food shot keeps its ordinary one");
        assertTrue(view.gui.ShotArt.rig(Plants.SNOW_PEA, Projectile.Kind.FROST, "fed")
                        .contains("T_SNOW_PEA"),
                "SNOWPEA_PLANTFOOD is an aura, not a projectile, and must not be picked up");
        assertTrue(view.gui.ShotArt.rig(Plants.GOO_PEASHOOTER, Projectile.Kind.GOO, "fed")
                        .contains("GOOPEASHOOTER_PLANTFOOD"),
                "a fed Goo Peashooter fires its big goo pea");
    }

    @Test
    void everyShotVariantThatNamesArtIsBundled() {
        for (Plants type : Plants.values()) {
            if (type.getCategory() != PlantsCategory.SHOOTER
                    || type == Plants.APPEASE_MINT) {
                continue;
            }
            Projectile.Kind kind = Projectile.kindOf(type);
            for (String variant : new String[] {"", "fed"}) {
                String rig = view.gui.ShotArt.rig(type, kind, variant);
                assertTrue(new java.io.File("assets/pvz/IMAGES/" + rig).exists(),
                        type.getName() + " (" + variant + ") wants a rig that is not "
                                + "in the bundle: " + rig);
            }
        }
    }

    @Test
    void theShroomsFireTheirOwnSporesAndTheColouredPeasShareThePeaRig() {
        assertTrue(view.gui.ShotArt.rig(Plants.SNOW_PEA, Projectile.Kind.FROST)
                        .contains("T_SNOW_PEA"),
                "the snow pea has its own rig and must not borrow the green pea");
        assertTrue(view.gui.ShotArt.rig(Plants.FIRE_PEASHOOTER, Projectile.Kind.FIRE)
                        .contains("T_FIRE_PEA"),
                "the fire pea has its own rig too");
        assertTrue(view.gui.ShotArt.rig(Plants.SEA_SHROOM, Projectile.Kind.SPORE)
                        .contains("SEASHROOM"),
                "Sea-shroom has its own projectile and must not borrow Puff-shroom's");
        assertTrue(view.gui.ShotArt.rig(Plants.PUFF_SHROOM, Projectile.Kind.SPORE)
                        .contains("PUFFSHROOM"),
                "Puff-shroom fires its own spore");
    }

    @Test
    void threepeaterSendsOnePeaIntoEachOfTheThreeLanes() {
        Game game = lawn();
        plant(game, Plants.THREEPEATER, 1, 2);
        fillRows(game, 7);
        GameLoop loop = new GameLoop(game);
        java.util.Set<Integer> lanes = new HashSet<Integer>();
        for (int i = 0; i < 12 * Game.TICKS_PER_SECOND; i++) {
            loop.step(game);
            clearTombs(game);
            for (Projectile shot : game.getProjectiles()) {
                lanes.add(Integer.valueOf(shot.getRow()));
            }
        }
        assertEquals(new HashSet<Integer>(java.util.Arrays.asList(
                        Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))),
                lanes, "up goes up, mid stays, down goes down");
    }

    @Test
    void aThreepeaterOnTheTopLaneOnlyFiresTwo() {
        Game game = lawn();
        plant(game, Plants.THREEPEATER, 1, 0);
        fillRows(game, 7);
        GameLoop loop = new GameLoop(game);
        java.util.Set<Integer> lanes = new HashSet<Integer>();
        for (int i = 0; i < 12 * Game.TICKS_PER_SECOND; i++) {
            loop.step(game);
            clearTombs(game);
            for (Projectile shot : game.getProjectiles()) {
                lanes.add(Integer.valueOf(shot.getRow()));
            }
        }
        assertEquals(2, lanes.size(),
                "there is no lane above the top one, so only two peas, saw " + lanes);
    }

    @Test
    void aFedShooterStopsTheMomentItsPlantFoodEnds() {
        for (Plants type : new Plants[] {Plants.REPEATER, Plants.MEGA_GATLING_PEA,
            Plants.PEASHOOTER}) {
            Game game = lawn();
            Plant made = plant(game, type, 1, 2);
            made.onPlantFood(game);
            GameLoop loop = new GameLoop(game);
            for (int i = 0; i < 30 * Game.TICKS_PER_SECOND; i++) {
                loop.step(game);
                clearTombs(game);
            }
            assertTrue(!made.isFed(), type.getName() + " is still fed long afterwards");
            for (Projectile shot : game.getProjectiles()) {
                assertTrue(!Projectile.FED.equals(shot.getVariant()),
                        type.getName() + " is still firing plant-food shots after it ended");
            }
        }
    }

    @Test
    void aPlantAttacksZombiesThatWereAlreadyThereWhenItWasPlanted() {
        Game game = lawn();
        fillRows(game, 6);
        Plant peashooter = plant(game, Plants.PEASHOOTER, 1, 2);
        GameLoop loop = new GameLoop(game);
        boolean fired = false;
        for (int i = 0; i < 12 * Game.TICKS_PER_SECOND && !fired; i++) {
            loop.step(game);
            clearTombs(game);
            fired = !game.getProjectiles().isEmpty();
        }
        assertTrue(fired, "a plant must shoot zombies that were on the lawn before it");
        assertTrue(peashooter.getHp() > 0d, "and it should still be standing");
    }

    @Test
    void rotobagaActuallyProducesShots() {
        Game game = lawn();
        plant(game, Plants.ROTOBAGA, 2, 2);
        fillRows(game, 6);
        GameLoop loop = new GameLoop(game);
        int seen = 0;
        for (int i = 0; i < 20 * Game.TICKS_PER_SECOND && seen == 0; i++) {
            loop.step(game);
            clearTombs(game);
            seen = game.getProjectiles().size();
        }
        assertTrue(seen > 0, "Rotobaga fired nothing at all");
    }

    @Test
    void aFedPeaPodThrowsFiveGiantPeasFromOneMouth() {
        Game game = lawn();
        Plant pod = plant(game, Plants.PEA_POD, 1, 2);
        pod.onPlantFood(game);
        GameLoop loop = new GameLoop(game);
        int fed = 0;
        java.util.Set<String> ports = new HashSet<String>();
        for (int i = 0; i < 4 * Game.TICKS_PER_SECOND; i++) {
            int before = game.getProjectiles().size();
            loop.step(game);
            clearTombs(game);
            for (int at = before; at < game.getProjectiles().size(); at++) {
                Projectile shot = game.getProjectiles().get(at);
                if (Projectile.FED.equals(shot.getVariant())) {
                    fed++;
                    ports.add(shot.getPort());
                }
            }
        }
        assertTrue(fed >= 5, "a fed Pea Pod should throw five giant peas, saw " + fed);
        assertEquals(java.util.Collections.singleton("head1"), ports,
                "all five leave the same mouth");
    }

    @Test
    void bowlingBulbRollsOneBouncingBulb() {
        Game game = lawn();
        plant(game, Plants.BOWLING_BULB, 1, 2);
        fillRows(game, 7);
        GameLoop loop = new GameLoop(game);
        boolean bounced = false;
        for (int i = 0; i < 20 * Game.TICKS_PER_SECOND && !bounced; i++) {
            loop.step(game);
            clearTombs(game);
            for (Projectile shot : game.getProjectiles()) {
                bounced |= shot.isDiagonal();
            }
        }
        assertTrue(bounced, "a bowling bulb should bounce between the lanes");
        assertTrue(view.gui.ShotArt.rig(Plants.BOWLING_BULB, Projectile.Kind.ORB,
                        Projectile.BULB + 1).contains("BOWLINGBULB_PROJECTILE1"),
                "it should use the first bulb's art");
    }

    @Test
    void splitPeaOnlyFiresBackwardsWhenSomethingIsBehindIt() {
        Game ahead = lawn();
        plant(ahead, Plants.SPLIT_PEA, 4, 2);
        ahead.getZombies().add(ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, 2, 7,
                ChapterType.ANCIENT_EGYPT));
        assertEquals(java.util.Collections.singleton(
                        model.entities.plants.types.SplitPea.FRONT),
                portsFired(ahead, 4d),
                "with nothing behind it, only the front head should fire");

        Game behind = lawn();
        plant(behind, Plants.SPLIT_PEA, 4, 2);
        behind.getZombies().add(ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, 2, 7,
                ChapterType.ANCIENT_EGYPT));
        behind.getZombies().add(ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, 2, 1,
                ChapterType.ANCIENT_EGYPT));
        assertEquals(2, portsFired(behind, 4d).size(),
                "with a zombie behind, both heads should fire");
    }

    @Test
    void aRepeatersSecondPeaLeavesLaterInTheSameCycle() {
        Game game = lawn();
        plant(game, Plants.REPEATER, 2, 2);
        fillRows(game, 8);
        GameLoop loop = new GameLoop(game);
        int bothAtOnce = 0;
        int oneThenTwo = 0;
        int seen = 0;
        for (int i = 0; i < 40 * Game.TICKS_PER_SECOND; i++) {
            int before = game.getProjectiles().size();
            loop.step(game);
            clearTombs(game);
            int born = game.getProjectiles().size() - before;
            if (born >= 2) {
                bothAtOnce++;
            } else if (born == 1) {
                oneThenTwo++;
            }
            seen += Math.max(0, born);
        }
        assertTrue(seen > 4, "the Repeater should have fired several times, saw " + seen);
        assertEquals(0, bothAtOnce,
                "a Repeater's two peas must leave on different ticks of the cycle");
        assertTrue(oneThenTwo >= 4, "each pea should arrive on its own tick");
    }

    @Test
    void aMuzzleFiresOnItsOwnFrameOfTheCycle() {
        MuzzleTiming.reset();
        Game game = lawn();
        Plant pod = plant(game, Plants.PEA_POD, 2, 2);
        ((model.entities.plants.types.PeaPod) pod).addHead();
        fillRows(game, 8);
        GameLoop loop = new GameLoop(game);
        java.util.Map<String, Integer> firstTick = new java.util.HashMap<String, Integer>();
        for (int tick = 0; tick < 12 * Game.TICKS_PER_SECOND; tick++) {
            loop.step(game);
            clearTombs(game);
            for (Projectile shot : game.getProjectiles()) {
                if (!firstTick.containsKey(shot.getPort())) {
                    firstTick.put(shot.getPort(), Integer.valueOf(tick));
                }
            }
        }
        assertTrue(firstTick.containsKey("head1") && firstTick.containsKey("head2"),
                "both grown heads should fire, saw " + firstTick.keySet());
        assertTrue(firstTick.get("head1").intValue() != firstTick.get("head2").intValue(),
                "the two heads must fire on different ticks, not together");
    }

    @Test
    void aTunedFrameOverridesTheDeclaredOne() {
        Muzzle muzzle = new Muzzle("probe", 0, 1, 0.35d);
        assertEquals(0.35d, muzzle.frameIn(Plants.PEASHOOTER, ""), 0.0001d,
                "an untuned muzzle keeps the frame its plant declared");
        MuzzleTiming.set(Plants.PEASHOOTER, "probe", "", 0.6d);
        assertEquals(0.6d, muzzle.frameIn(Plants.PEASHOOTER, ""), 0.0001d,
                "a tuned frame should win");
        MuzzleTiming.reset();
    }
}
