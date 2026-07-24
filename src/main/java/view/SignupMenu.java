package view;

import controller.menu.SignupMenuController;
import model.Result;
import model.enums.commands.SignUpCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class SignupMenu implements AppMenu {
    private final SignupMenuController controller;

    public SignupMenu() {
        this.controller = new SignupMenuController();
    }

    @Override
    public void check(Scanner scanner) {
        String line = scanner.nextLine().trim();
        if (SignUpCommands.EXIT_MENU_REGEX.matches(line)) {
            Result result = controller.exitMenu();
            System.out.println(result.message());
        } else if (SignUpCommands.ENTER_MENU_REGEX.matches(line)) {
            handleEnterMenu(line);
        } else if (SignUpCommands.CURRENT_MENU_REGEX.matches(line)) {
            Result result = controller.showCurrentMenu();
            System.out.println(result.message());
        } else if (SignUpCommands.REGISTER_REGEX.matches(line)) {
            handleRegister(line);
        } else if (SignUpCommands.PICK_SECURITY_QUESTION_REGEX.matches(line)) {
            handlePickQuestion(line);
        } else invalidCommand();
    }

    public void handleRegister(String input) {
        StringBuilder sb = new StringBuilder();
        Result result;
        Matcher matcher = SignUpCommands.REGISTER_REGEX.getMatcher(input);
        String username = matcher.group("username");
        result = controller.setUsername(username);
        sb.append(result.message());
        String password = matcher.group("password");
        result = controller.setPassword(password);
        sb.append(result.message());
        String passwordConfirm = matcher.group("passwordConfirm");
        result = controller.setPasswordConfirm(passwordConfirm, password);
        sb.append(result.message());
        String nickname = matcher.group("nickname");
        result = controller.setNickname(nickname);
        sb.append(result.message());
        String email = matcher.group("email");
        result = controller.setEmail(email);
        sb.append(result.message());
        String gender = matcher.group("gender");
        if (controller.isRegisterValid()) {
            result = controller.setGender(gender);
            sb.append(result.message());
        }
        System.out.println(sb);
    }

    public void handleEnterMenu(String input) {
        Matcher matcher = SignUpCommands.ENTER_MENU_REGEX.getMatcher(input);
        String menuName = matcher.group("menuName");
        Result result = controller.enterMenu(menuName);
        System.out.println(result.message());
    }

    public void handlePickQuestion(String input) {
        Matcher matcher = SignUpCommands.PICK_SECURITY_QUESTION_REGEX.getMatcher(input);
        String questionNum = matcher.group(1).trim();
        String answer = matcher.group(2).trim().toLowerCase();
        String answerConfirm = matcher.group(3).trim();
        Result result = controller.setQuestion(questionNum, answer, answerConfirm);
        System.out.println(result.message());
    }
}
