package model.enums.commands;

public enum TravelLogCommands implements Commands {
    TRAVEL_LOG_PAGE("^\\s*travel\\s+log\\s+page\\s+(?<page>\\S+)\\s*$"),
    SHOW_QUESTS("^\\s*show\\s+quests\\s*$"),
    CURRENT_MENU("^\\s*menu\\s+show\\s+current\\s*$"),
    EXIT_MENU("^\\s*menu\\s+exit\\s*$");

    private final String regex;

    TravelLogCommands(String regex) {
        this.regex = regex;
    }

    @Override
    public String getPattern() {
        return regex;
    }
}
