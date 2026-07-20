package controller;

import database.UserRepository;
import model.App;
import model.Result;
import model.User;
import model.enums.Menu;
import model.enums.SecurityQuestions;

import model.enums.commands.SignUpCommands;

public class SignupMenuController {

    private final SignUpValidation validation;
    private boolean isRegisterValid;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private String gender;
    private String answer;

    public UserRepository repository;

    private int securityQuestionNum;

    public SignupMenuController() {
        this.validation = new SignUpValidation();
        this.repository=new UserRepository();
        this.isRegisterValid = true;
    }

    public boolean isRegisterValid() {
        return isRegisterValid;
    }

    public Result setUsername(String username) {
        isRegisterValid = true;
        if (!validation.isUsernameValid(username)) {
            isRegisterValid = false;
            return new Result(false,
                "Username can only contain A-Za-z letters, numbers, and the symbol -.\n", null);
        }
        if (repository.usernameExists(username)) {
            isRegisterValid = false;
            return new Result(false, "Username already exists!\n", null);
        }
        this.username = username;
        return new Result(true, "", null);
    }

    public Result setPassword(String pass) {
        if (!validation.isPasswordValid(pass)) {
            isRegisterValid = false;
            return new Result(false,
                "Password can only contain A-Za-z letters, numbers, and special symbols.\n", null);
        }
        if (!validation.isPasswordStrong(pass)) {
            isRegisterValid = false;
            StringBuilder sb = new StringBuilder();
            sb.append("Password is too weak.\n");
            if (!validation.isWeakPasswordLongEnough(pass))
                sb.append("It must be at least 8 characters long!\n");
            if (!validation.hasWeakPasswordUpperCaseLetter(pass))
                sb.append("It must contain at least one uppercase letter.\n");
            if (!validation.hasWeakPasswordLowerCaseLetter(pass))
                sb.append("It must contain at least one lowercase letter.\n");
            if (!validation.hasWeakPasswordNum(pass))
                sb.append("It must contain at least one number.\n");
            if (!validation.hasWeakPasswordSpecialSymbol(pass))
                sb.append("It must contain at least one special symbol.\n");
            return new Result(false, sb.toString(), null);
        }
        this.passwordHash = HashUtil.hashPassword(pass);
        return new Result(true, "", null);
    }

    public Result setPasswordConfirm(String passConfirm, String pass) {
        if (!validation.are2passwordsSame(passConfirm, pass)) {
            isRegisterValid = false;
            return new Result(false, "Passwords do not match. Try again or return back.\n", null);
        }
        if (this.passwordHash == null) {
            this.passwordHash = HashUtil.hashPassword(pass);
        }
        return new Result(true, "", null);
    }

    public Result setNickname(String nickname) {
        if (!validation.isNicknameLengthValid(nickname)) {
            isRegisterValid = false;
            return new Result(false, "Nickname length must be between 3 and 30 characters.\n", null);
        }
        this.nickname = nickname;
        return new Result(true, "", null);
    }

    public Result setEmail(String email) {
        if (!validation.hasExactlyOneAtSign(email)) {
            isRegisterValid = false;
            return new Result(false, "Email must contain exactly one @.\n", null);
        }
        if (validation.hasInvalidChar(email)) {
            isRegisterValid = false;
            return new Result(false, "Email contains invalid characters.\n", null);
        }
        String[] parts = email.split("@");
        if (!validation.isFirstPartEmailValid(parts[0])) {
            isRegisterValid = false;
            return new Result(false,
                "The first part of email must start and end with a letter or digit, " +
                    "may only contain letters, digits, '_', '-' and '.' (not consecutive)\n", null);
        }
        if (!validation.isSecondPartEmailValid(parts[1])) {
            isRegisterValid = false;
            return new Result(false,
                "The second part of email must start and end with a letter or digit " +
                    "and be separated by dots.\n", null);
        }
        this.email = email;
        return new Result(true, "", null);
    }

    public Result setGender(String gender) {
        if (!validation.isGenderValid(gender)) {
            isRegisterValid = false;
            return new Result(false, "Please select a valid gender\n", null);
        }
        this.gender = gender;
        return new Result(true,
            "Please choose a security question and enter an answer without spaces.\n"
                + SecurityQuestions.listOfSecurityQuestions(), null);
    }

    public Result setQuestion(String questionNum, String answer, String answerConfirm) {
        if (!validation.isQuestionNumValid(questionNum)) {
            isRegisterValid = false;
            return new Result(false, "Please enter a number from 1 to " + SecurityQuestions.getCount() + ".\n", null);
        }
        if (!validation.are2answersSame(answer, answerConfirm)) {
            isRegisterValid = false;
            return new Result(false, "The answers do not match.\n", null);
        }

        this.securityQuestionNum = validation.questionNum;
        this.answer = HashUtil.hashPassword(answer.trim().toLowerCase());

        if (!allFieldsReady()) {
            isRegisterValid = false;
            return new Result(false, "Some registration fields are missing.\n", null);
        }

        User user = new User(username, passwordHash, nickname, email, gender, securityQuestionNum, this.answer);
        boolean success = repository.register(user);
        if (!success) {
            isRegisterValid = false;
            return new Result(false, "Registration failed\n", null);
        }

        resetFields();
        return new Result(true, "Registration completed successfully.\n", null);
    }

    private boolean allFieldsReady() {
        return validation.isNotBlank(username) &&
            validation.isNotBlank(passwordHash) &&
            validation.isNotBlank(nickname) &&
            validation.isNotBlank(email) &&
            validation.isNotBlank(gender) &&
            validation.isNotBlank(answer);
    }

    private void resetFields() {
        isRegisterValid = true;
        username = null;
        passwordHash = null;
        nickname = null;
        email = null;
        gender = null;
        securityQuestionNum = 0;
        answer = null;
    }

    public Result exitMenu() {
        System.exit(0);
        return null;
    }

    public Result showCurrentMenu() {
        return new Result(true, "You are now in the signup menu.\n", null);
    }

    public Result enterMenu(String menuName) {
        String err = Navigation.enter(App.getInstance(), menuName);
        if (err != null) return new Result(false, err + "\n", null);
        return new Result(true, "", null);
    }

    public static class SignUpValidation {

        public int questionNum;

        public boolean isUsernameValid(String username) {
            return SignUpCommands.USERNAME_REGEX.matches(username);
        }

        public boolean isPasswordValid(String password) {
            return SignUpCommands.PASSWORD_REGEX.matches(password);
        }

        public boolean isPasswordStrong(String password) {
            return SignUpCommands.STRONG_PASSWORD_REGEX.matches(password);
        }

        public boolean isWeakPasswordLongEnough(String password) {
            return password.length() >= 8;
        }

        public boolean hasWeakPasswordUpperCaseLetter(String password) {
            return password.matches(".*[A-Z].*");
        }

        public boolean hasWeakPasswordLowerCaseLetter(String password) {
            return password.matches(".*[a-z].*");
        }

        public boolean hasWeakPasswordNum(String password) {
            return password.matches(".*\\d.*");
        }

        public boolean hasWeakPasswordSpecialSymbol(String password) {
            return SignUpCommands.SPECIAL_SYMBOLS_REGEX.matches(password);
        }

        public boolean are2passwordsSame(String pass, String pass2) {
            return pass.equals(pass2);
        }

        public boolean isNicknameLengthValid(String username) {
            return username.length() >= 3 && username.length() <= 30;
        }

        public boolean hasExactlyOneAtSign(String email) {
            return email.matches("^[^@]+@[^@]+$");
        }

        public boolean isFirstPartEmailValid(String firstPart) {
            return SignUpCommands.EMAIL_FIRST_PART_REGEX.matches(firstPart);
        }

        public boolean isSecondPartEmailValid(String secondPart) {
            return SignUpCommands.EMAIL_SECOND_PART_REGEX.matches(secondPart);
        }

        public boolean hasInvalidChar(String email) {
            return email.matches("^.*[^a-zA-Z0-9._@-].*$");
        }

        public boolean isGenderValid(String gender) {
            return gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female");
        }

        public boolean isQuestionNumValid(String qNum) {
            try {
                questionNum = Integer.parseInt(qNum);
                return questionNum >= 1 && questionNum <= SecurityQuestions.getCount();
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public boolean are2answersSame(String answer, String answer2) {
            return answer.equalsIgnoreCase(answer2);
        }

        public boolean isNotBlank(String s) {
            return s != null && !s.trim().isEmpty();
        }
    }
}
