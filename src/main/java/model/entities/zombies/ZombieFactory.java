package model.entities.zombies;

import model.ChapterType;
import model.Vec2;
import model.entities.zombies.types.*;

public abstract class ZombieFactory {

    public static Zombie create(Zombies data, int line, int col, ChapterType chapter) {
        return create(data, line, new Vec2(col, line), chapter, null);
    }

    public static Zombie create(Zombies data, int line, Vec2 position, ChapterType chapter, ZombieType type) {
        switch (data) {
            case ZOMBIE_GARGANTUAR:
                return new Gargantuar(line, position, chapter, type);
            case ZOMBIE_IMP:
                return new Imp(line, position, chapter, type);
            case ZOMBIE_DARK_IMP_DRAGON:
                return new ImpDragon(line, position, chapter, type);
            case ZOMBIE_DARK_JUGGLER:
                return new Juggler(line, position, chapter, type);
            case ZOMBIE_ICE_AGE_DODO:
                return new DodoRider(line, position, chapter, type);
            case ZOMBIE_BARREL_ROLLER:
                return new BarrelRoller(line, position, chapter, type);
            case ZOMBIE_ARCADE:
                return new ArcadeZombie(line, position, chapter, type);
            case ZOMBIE_NEWSPAPER:
                return new NewspaperZombie(line, position, chapter, type);
            case ZOMBIE_ICE_AGE_TROGLOBITE:
                return new Troglobite(line, position, chapter, type);
            case ZOMBIE_LOST_CITY_JANE:
                return new ParasolZombie(line, position, chapter, type);
            case ZOMBIE_CRYSTAL_SKULL:
                return new TurquoiseZombie(line, position, chapter, type);
            case ZOMBIE_PROSPECTOR:
                return new ProspectorZombie(line, position, chapter, type);
            case ZOMBIE_PIANO:
                return new PianistZombie(line, position, chapter, type);
            case ZOMBIE_RA:
                return new RaZombie(line, position, chapter, type);
            case ZOMBIE_EXPLORER:
                return new ExplorerZombie(line, position, chapter, type);
            case ZOMBIE_ICE_AGE_HUNTER:
                return new HunterZombie(line, position, chapter, type);
            case ZOMBIE_BEACH_OCTOPUS:
                return new OctopusZombie(line, position, chapter, type);
            case ZOMBIE_WIZARD:
                return new WizardZombie(line, position, chapter, type);
            case ZOMBIE_PEASHOOTER_HEAD:
                return new PeashooterZombie(line, position, chapter, type);
            case ZOMBIE_WALLNUT_HEAD:
                return new WallnutZombie(line, position, chapter, type);
            case ZOMBIE_JALAPENO_HEAD:
                return new JalapenoZombie(line, position, chapter, type);
            case ZOMBIE_SQUASH_HEAD:
                return new SquashZombie(line, position, chapter, type);

            default:
                return new BasicZombie(data, line, position, chapter, type);
        }
    }
}
