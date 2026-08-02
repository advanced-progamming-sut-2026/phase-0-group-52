package model.mechanics;

import model.ChapterType;
import model.Game;

public interface ChapterMechanics {

    void onWaveStart(Game game);

    void onTick(Game game);

    static ChapterMechanics forChapter(ChapterType chapter) {
        switch (chapter) {
            case ANCIENT_EGYPT:   return new AncientEgyptMechanics();
            case FROSTBITE_CAVES: return new FrostbiteCavesMechanics();
            case DARK_AGES:       return new DarkAgesMechanics();
            case BIG_WAVE_BEACH:  return new BigWaveBeachMechanics();
            default:              return null;
        }
    }
}
