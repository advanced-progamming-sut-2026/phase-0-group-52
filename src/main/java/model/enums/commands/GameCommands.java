package model.enums.commands;

public enum GameCommands implements Commands {
    ADVANCE_TIME("^\\s*advance\\s+time\\s+-t\\s+(?<count>\\d+)\\s+ticks?\\s*$"),
    COLLECT_SUN("^\\s*collect\\s+sun\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),
    SHOW_SUN("^\\s*show\\s+sun\\s+amount\\s*$"),
    CHEAT_ADD_SUN("^\\s*cheat\\s+add\\s+-n\\s+(?<count>\\d+)\\s+suns?\\s*$"),
    PLANT_PLANT("^\\s*plant\\s+plant\\s+-t\\s+(?<type>\\S+)\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),
    PLUCK_PLANT("^\\s*pluck\\s+plant\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),
    FEED_PLANT("^\\s*feed\\s+plant\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),
    CHEAT_REMOVE_COOLDOWN("^\\s*cheat\\s+remove-cooldown\\s*$"),
    CHEAT_ADD_PLANT_FOOD("^\\s*cheat\\s+add-plant-food\\s*$"),
    SHOW_MAP("^\\s*show\\s+map\\s*$"),
    SHOW_PLANTS_STATUS("^\\s*show\\s+plants\\s+status\\s*$"),
    SHOW_TILE_STATUS("^\\s*show\\s+tile\\s+status\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),
    RELEASE_NUKE("^\\s*release\\s+the\\s+nuke\\s*$"),
    CURRENT_MENU("^\\s*menu\\s+show\\s+current\\s*$"),
    EXIT_MENU("^\\s*menu\\s+exit\\s*$");

    private final String regex;

    GameCommands(String regex) {
        this.regex = regex;
    }

    @Override
    public String getPattern() {
        return regex;
    }
}
