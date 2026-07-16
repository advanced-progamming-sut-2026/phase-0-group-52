package pvz.minigame;

import pvz.model.User;

import java.util.Scanner;

public class Minigame {

    private final MinigameType type;
    private final int level;

    public Minigame(MinigameType type, int level) {
        this.type = type;
        this.level = Math.max(1, Math.min(3, level));
    }

    public MinigameType getType() { return type; }
    public int getLevel() { return level; }

    public static MinigameType findType(String name) {
        String normalized = name.replace("_", "").replace("-", "");
        for (MinigameType type : MinigameType.values())
            if (type.name().replace("_", "").equalsIgnoreCase(normalized)) return type;
        return null;
    }

    public void start(User user, Scanner scanner) {
        switch (type) {
            case BEGHOULED:
                new Beghouled(level, user).run(scanner);
                break;
            default:
                System.out.println(type + " level " + level
                        + " needs the realtime game engine and will be playable in the next phase.");
        }
    }
}
