package pvz.model.mechanics;

import pvz.model.ChapterType;
import pvz.model.Game;

public interface ChapterMechanics {

    void onWaveStart(Game game);

    void onTick(Game game);

    static ChapterMechanics forChapter(ChapterType chapter) {
        switch (chapter) {
            case FROSTBITE_CAVES: return new FrostbiteCavesMechanics();
            case DARK_AGES:       return new DarkAgesMechanics();
            default:              return null;
        }
    }
}
