package pvz.minigame;

import pvz.model.*;

public class Minigame {
    private MinigameType type;
    private String rules;

    public Minigame(MinigameType type, String rules) {
        this.type = type;
        this.rules = rules;
    }
}
