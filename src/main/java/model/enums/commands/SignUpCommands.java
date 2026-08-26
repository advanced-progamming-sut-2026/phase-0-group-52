package model.enums.commands;

public enum SignUpCommands implements Commands {


    USERNAME_REGEX("^[a-zA-Z0-9\\-]+$"),
    NICKNAME_REGEX("^.{3,30}$"),
    PASSWORD_REGEX("^[a-zA-Z0-9!#$%^&*()=+{}\\[\\]|/:;'\",<>?\\\\]+$"),
    STRONG_PASSWORD_REGEX("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!#$%^&*()=+{}\\[\\]|/:;'\",<>?\\\\])[a-zA-Z0-9!#$%^&*()=+{}\\[\\]|/:;'\",<>?\\\\]{8,}$"),

    SPECIAL_SYMBOLS_REGEX("^.*[!#$%^&*()=+{}\\[\\]|/:;'\",<>?\\\\].*$"),

    EMAIL_REGEX("^(?!.*\\.\\.)[a-zA-Z0-9](?:[a-zA-Z0-9.\\-_]*[a-zA-Z0-9])?@" +
        "[a-zA-Z0-9\\-]+(?:\\.[a-zA-Z0-9\\-]+)*\\.[a-zA-Z]{2,}$"),

    EMAIL_FIRST_PART_REGEX("^(?!.*\\.\\.)[a-zA-Z0-9](?:[a-zA-Z0-9._-]*[a-zA-Z0-9])?$"),

    EMAIL_SECOND_PART_REGEX("^(?!.*\\.\\.)[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?" +
        "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$"),

    EXIT_MENU_REGEX ("^\\s*menu\\s+exit\\s*$"),
    CURRENT_MENU_REGEX ("^\\s*menu\\s+show\\s+current\\s*$"),
    ENTER_MENU_REGEX("^\\s*menu\\s+enter\\s+(?<menuName>\\S+)\\s*$"),
    MENU_NAME_REGEX("^(?i)(Main|Game|Login|SignUp|Setting|Network|News|Profile|Collection)$");

    private final String regex;

    SignUpCommands(String regex) {
        this.regex = regex;
    }

    @Override
    public String getPattern() {
        return regex;
    }
}
