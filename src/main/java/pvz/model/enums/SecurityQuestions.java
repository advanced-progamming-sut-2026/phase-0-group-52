package pvz.model.enums;

public enum SecurityQuestions {
    FAVORITE_MOVIE(1,"What is your favorite movie of all time?"),
    DREAM_JOB(2,"What job did you dream of as a child?"),
    BEST_VACATION(3,"Where did you go on your best vacation?"),
    FAVORITE_BOOK(4,"What is your favorite book?"),
    FIRST_CONCERT(5,"Who was the first artist you saw in concert?"),
    CHILDHOOD_HERO(6,"Who was your childhood hero?"),
    FAVORITE_FOOD(7,"What is your favorite food to cook?");

    private final int num;
    private final String question;

    SecurityQuestions(int num,String question){
        this.num=num;
        this.question=question;
    }

    public String getQuestion() {
        return question;
    }

    public static String getQuestionByIndex(int index) {
        SecurityQuestions[] questions = values();
        if (index < 1 || index > questions.length) {
            return null;
        }
        return questions[index - 1].getQuestion();
    }

    public static SecurityQuestions getByIndex(int index) {
        SecurityQuestions[] questions = values();
        if (index < 1 || index > questions.length) {
            return null;
        }
        return questions[index - 1];
    }

    public static String listOfSecurityQuestions(){
        StringBuilder sb = new StringBuilder();
        for(SecurityQuestions sq: SecurityQuestions.values()){
            sb.append(sq.num).append(".").append(sq.question).append("\n");
        }
        return sb.toString();
    }

    public static int getCount() {
        return values().length;
    }

    public static String[] getAllQuestions() {
        SecurityQuestions[] questions = values();
        String[] result = new String[questions.length];
        for (int i = 0; i < questions.length; i++) {
            result[i] = questions[i].getQuestion();
        }
        return result;
    }
}
