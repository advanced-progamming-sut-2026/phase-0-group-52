package pvz.model.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Commands {


    String getPattern();
    default Matcher getMatcher(String input) {
        Matcher matcher = Pattern.compile(getPattern()).matcher(input);
        if (matcher.matches()) {
            return matcher;
        }
        return null;
    }
    default boolean matches(String input) {
        return Pattern.compile(getPattern()).matcher(input).matches();
    }
    default String getGroup(String input, String group) {
        return getMatcher(input).group(group);
    }
}
