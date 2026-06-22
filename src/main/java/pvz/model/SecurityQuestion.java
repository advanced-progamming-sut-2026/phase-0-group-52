package pvz.model;

import java.util.Arrays;
import java.util.List;

public class SecurityQuestion {

    private static final List<String> QUESTIONS = Arrays.asList(
        "What was the name of your first pet?",
        "What city were you born in?",
        "What was the name of your best childhood friend?",
        "What was the model of your first car?",
        "What is your favorite food?"
    );

    public static List<String> getQuestions() {
        return QUESTIONS;
    }

    public static String getQuestionByIndex(int index) {
        if (!isValidIndex(index)) {
            return null;
        }
        return QUESTIONS.get(index - 1);
    }

    public static boolean isValidIndex(int index) {
        return index >= 1 && index <= QUESTIONS.size();
    }

    public static int getCount() {
        return QUESTIONS.size();
    }
}
