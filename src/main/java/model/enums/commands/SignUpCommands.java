package model.enums.commands;

public enum SignUpCommands implements Commands {

    REGISTER_REGEX("^\\s*register\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)\\s+(?<passwordConfirm>\\S+)" +
        "\\s+-n\\s+(?<nickname>\\S+)\\s+-e\\s+(?<email>\\S+)\\s+-g\\s+(?<gender>\\S+)\\s*$"),
    PICK_SECURITY_QUESTION_REGEX("^\\s*pick\\s+question\\s+-q\\s+(?<questionNumber>\\S+)\\s+-a\\s+(?<answer>\\S+)\\s+-c\\s+(?<answerConfirm>\\S+)\\s*$"),

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
