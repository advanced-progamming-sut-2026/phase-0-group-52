package model.enums.commands;

public enum LoginCommands implements Commands{
    LOGIN_REGEX("^\\s*login\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)(?<stay>\\s+-stay-logged-in)?\\s*$"),
    FORGET_PASSWORD_REGEX("^\\s*forget\\s+password\\s+-u\\s+(\\S+)\\s+-e\\s+(\\S+)\\s*$"),
    ANSWER_QUESTION_REGEX("^\\s*answer\\s+-a\\s+(\\S+)\\s*$"),
    EXIT_MENU_REGEX ("^\\s*menu\\s+exit\\s*$"),
    CURRENT_MENU_REGEX ("^\\s*menu\\s+show\\s+current\\s*$"),
    ENTER_MENU_REGEX("^\\s*menu\\s+enter\\s+(?<menuName>\\S+)\\s*$"),
    MENU_NAME_REGEX("^(?i)(Main|Game|Login|SignUp|Setting|Network|News|Profile|Collection)$");
    private final String regex;
    LoginCommands(String regex) {this.regex = regex;}
    @Override
    public String getPattern() {
        return regex;
    }
}
