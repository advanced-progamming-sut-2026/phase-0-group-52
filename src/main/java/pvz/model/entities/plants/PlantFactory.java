package pvz.model.entities.plants;

import pvz.model.Vec2;
import pvz.model.entities.plants.types.BowlingBulb;
import pvz.model.entities.plants.types.Cactus;
import pvz.model.entities.plants.types.CherryBomb;
import pvz.model.entities.plants.types.Citron;
import pvz.model.entities.plants.types.DoomShroom;
import pvz.model.entities.plants.types.Endurian;
import pvz.model.entities.plants.types.ExplodeONut;
import pvz.model.entities.plants.types.FirePeashooter;
import pvz.model.entities.plants.types.FumeShroom;
import pvz.model.entities.plants.types.Garlic;
import pvz.model.entities.plants.types.GooPeashooter;
import pvz.model.entities.plants.types.Grapeshot;
import pvz.model.entities.plants.types.GraveBuster;
import pvz.model.entities.plants.types.HotPotato;
import pvz.model.entities.plants.types.HypnoShroom;
import pvz.model.entities.plants.types.IceShroom;
import pvz.model.entities.plants.types.IcebergLettuce;
import pvz.model.entities.plants.types.Imitater;
import pvz.model.entities.plants.types.Jalapeno;
import pvz.model.entities.plants.types.LilyPad;
import pvz.model.entities.plants.types.MegaGatlingPea;
import pvz.model.entities.plants.types.PeaPod;
import pvz.model.entities.plants.types.Peashooter;
import pvz.model.entities.plants.types.PotatoMine;
import pvz.model.entities.plants.types.PrimalPotatoMine;
import pvz.model.entities.plants.types.PuffShroom;
import pvz.model.entities.plants.types.Pumpkin;
import pvz.model.entities.plants.types.Repeater;
import pvz.model.entities.plants.types.Rotobaga;
import pvz.model.entities.plants.types.SeaShroom;
import pvz.model.entities.plants.types.SnowPea;
import pvz.model.entities.plants.types.SplitPea;
import pvz.model.entities.plants.types.Squash;
import pvz.model.entities.plants.types.Starfruit;
import pvz.model.entities.plants.types.SunBean;
import pvz.model.entities.plants.types.SweetPotato;
import pvz.model.entities.plants.types.TallNut;
import pvz.model.entities.plants.types.TangleKelp;
import pvz.model.entities.plants.types.Threepeater;
import pvz.model.entities.plants.types.Torchwood;
import pvz.model.entities.plants.types.WallNut;

public abstract class PlantFactory {

    public static Plant create(Plants type, Vec2 position) {
        switch (type) {

            case PEASHOOTER:        return new Peashooter(position);
            case REPEATER:          return new Repeater(position);
            case THREEPEATER:       return new Threepeater(position);
            case SNOW_PEA:          return new SnowPea(position);
            case ROTOBAGA:          return new Rotobaga(position);
            case PEA_POD:           return new PeaPod(position);
            case SPLIT_PEA:         return new SplitPea(position);
            case CITRON:            return new Citron(position);
            case BOWLING_BULB:      return new BowlingBulb(position);
            case FIRE_PEASHOOTER:   return new FirePeashooter(position);
            case STARFRUIT:         return new Starfruit(position);
            case GOO_PEASHOOTER:    return new GooPeashooter(position);
            case MEGA_GATLING_PEA:  return new MegaGatlingPea(position);
            case SEA_SHROOM:        return new SeaShroom(position);
            case PUFF_SHROOM:       return new PuffShroom(position);

            case CACTUS:            return new Cactus(position);
            case FUME_SHROOM:       return new FumeShroom(position);

            case TORCHWOOD:         return new Torchwood(position);
            case HYPNO_SHROOM:      return new HypnoShroom(position);
            case IMITATER:          return new Imitater(position);
            case LILY_PAD:          return new LilyPad(position);

            case POTATO_MINE:       return new PotatoMine(position);
            case PRIMAL_POTATO_MINE:return new PrimalPotatoMine(position);
            case CHERRY_BOMB:       return new CherryBomb(position);
            case SQUASH:            return new Squash(position);
            case GRAPESHOT:         return new Grapeshot(position);
            case JALAPENO:          return new Jalapeno(position);
            case DOOM_SHROOM:       return new DoomShroom(position);
            case TANGLE_KELP:       return new TangleKelp(position);
            case ICEBERG_LETTUCE:   return new IcebergLettuce(position);
            case ICE_SHROOM:        return new IceShroom(position);
            case HOT_POTATO:        return new HotPotato(position);
            case GRAVE_BUSTER:      return new GraveBuster(position);

            case WALL_NUT:          return new WallNut(position);
            case TALL_NUT:          return new TallNut(position);
            case ENDURIAN:          return new Endurian(position);
            case GARLIC:            return new Garlic(position);
            case SWEET_POTATO:      return new SweetPotato(position);
            case EXPLODE_O_NUT:     return new ExplodeONut(position);
            case PUMPKIN:           return new Pumpkin(position);
            case SUN_BEAN:          return new SunBean(position);
            default:
                return createByCategory(type, position);
        }
    }

    private static Plant createByCategory(Plants type, Vec2 position) {
        switch (type.getCategory()) {
            case SUN_PRODUCER:   return new SunProducer(type, position);
            case SHOOTER:        return isMint(type) ? new Mint(type, position) : new Shooter(type, position);
            case HOMING:         return new Homing(type, position);
            case STRIKE_THROUGH: return isMint(type) ? new Mint(type, position) : new StrikeThrough(type, position);
            case LOBBER:         return new Lobber(type, position);
            case EXPLOSIVE:      return isMint(type) ? new Mint(type, position) : new Explosive(type, position);
            case MELEE:          return new Melee(type, position);
            case MODIFIER:       return isMint(type) ? new Mint(type, position) : new Modifier(type, position);
            case MINT:           return new Mint(type, position);
            case WALL_NUT:       return isMint(type) ? new Mint(type, position) : new Wallnut(type, position);
            default:             return new Modifier(type, position);
        }
    }

    private static boolean isMint(Plants type) {
        return type.name().endsWith("_MINT") || type.name().endsWith("MINT");
    }
}
