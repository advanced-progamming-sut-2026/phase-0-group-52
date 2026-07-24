package model.entities.plants;

import model.Vec2;
import model.entities.plants.types.*;

public abstract class PlantFactory {

    public static Plant create(Plants type, Vec2 position) {
        switch (type) {

            case PEASHOOTER:
                return new Peashooter(position);
            case REPEATER:
                return new Repeater(position);
            case THREEPEATER:
                return new Threepeater(position);
            case SNOW_PEA:
                return new SnowPea(position);
            case ROTOBAGA:
                return new Rotobaga(position);
            case PEA_POD:
                return new PeaPod(position);
            case SPLIT_PEA:
                return new SplitPea(position);
            case CITRON:
                return new Citron(position);
            case BOWLING_BULB:
                return new BowlingBulb(position);
            case FIRE_PEASHOOTER:
                return new FirePeashooter(position);
            case STARFRUIT:
                return new Starfruit(position);
            case GOO_PEASHOOTER:
                return new GooPeashooter(position);
            case MEGA_GATLING_PEA:
                return new MegaGatlingPea(position);
            case SEA_SHROOM:
                return new SeaShroom(position);
            case PUFF_SHROOM:
                return new PuffShroom(position);

            case CACTUS:
                return new Cactus(position);
            case FUME_SHROOM:
                return new FumeShroom(position);

            case TORCHWOOD:
                return new Torchwood(position);
            case HYPNO_SHROOM:
                return new HypnoShroom(position);
            case IMITATER:
                return new Imitater(position);
            case LILY_PAD:
                return new LilyPad(position);

            case POTATO_MINE:
                return new PotatoMine(position);
            case PRIMAL_POTATO_MINE:
                return new PrimalPotatoMine(position);
            case CHERRY_BOMB:
                return new CherryBomb(position);
            case SQUASH:
                return new Squash(position);
            case GRAPESHOT:
                return new Grapeshot(position);
            case JALAPENO:
                return new Jalapeno(position);
            case DOOM_SHROOM:
                return new DoomShroom(position);
            case TANGLE_KELP:
                return new TangleKelp(position);
            case ICEBERG_LETTUCE:
                return new IcebergLettuce(position);
            case ICE_SHROOM:
                return new IceShroom(position);
            case HOT_POTATO:
                return new HotPotato(position);
            case GRAVE_BUSTER:
                return new GraveBuster(position);

            case WALL_NUT:
                return new WallNut(position);
            case TALL_NUT:
                return new TallNut(position);
            case ENDURIAN:
                return new Endurian(position);
            case GARLIC:
                return new Garlic(position);
            case SWEET_POTATO:
                return new SweetPotato(position);
            case EXPLODE_O_NUT:
                return new ExplodeONut(position);
            case PUMPKIN:
                return new Pumpkin(position);
            case SUN_BEAN:
                return new SunBean(position);
            default:
                return createByCategory(type, position);
        }
    }

    private static Plant createByCategory(Plants type, Vec2 position) {
        switch (type.getCategory()) {
            case SUN_PRODUCER:
                return new SunProducer(type, position);
            case SHOOTER:
                return isMint(type) ? new Mint(type, position) : new Shooter(type, position);
            case HOMING:
                return new Homing(type, position);
            case STRIKE_THROUGH:
                return isMint(type) ? new Mint(type, position) : new StrikeThrough(type, position);
            case LOBBER:
                return new Lobber(type, position);
            case EXPLOSIVE:
                return isMint(type) ? new Mint(type, position) : new Explosive(type, position);
            case MELEE:
                return new Melee(type, position);
            case MODIFIER:
                return isMint(type) ? new Mint(type, position) : new Modifier(type, position);
            case MINT:
                return new Mint(type, position);
            case WALL_NUT:
                return isMint(type) ? new Mint(type, position) : new Wallnut(type, position);
            default:
                return new Modifier(type, position);
        }
    }

    private static boolean isMint(Plants type) {
        return type.name().endsWith("_MINT") || type.name().endsWith("MINT");
    }
}
