package model.mechanics;

import model.ChapterType;
import model.Game;

public interface ChapterMechanics {

    static ChapterMechanics forChapter(ChapterType chapter) {
        switch (chapter) {
            case FROSTBITE_CAVES:
                return new FrostbiteCavesMechanics();
            case DARK_AGES:
                return new DarkAgesMechanics();
            default:
                return null;
        }
    }

    void onWaveStart(Game game);

    void onTick(Game game);
}
