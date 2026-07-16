package pvz.view;

import pvz.controller.LoginMenuController;
import pvz.model.Result;
import pvz.model.enums.commands.LoginCommands;
import java.util.Scanner;
import java.util.regex.Matcher;

public class LoginMenu implements AppMenu{
    private final LoginMenuController controller;
    public LoginMenu(){this.controller = new LoginMenuController();}
    @Override
    public void check(Scanner scanner ) {
        String line = scanner.nextLine().trim();
        if(controller.isWaitingForNewPassword()){
            Result result = controller.setNewPassword(line.trim());
            System.out.println(result.message());
        }
        else if(LoginCommands.LOGIN_REGEX.matches(line)){
            handleLogin(line);
        }
        else if(LoginCommands.EXIT_MENU_REGEX.matches(line)) {
            Result result = controller.exitMenu();
            System.out.println(result.message());
        }
        else if(LoginCommands.FORGET_PASSWORD_REGEX.matches(line)){
            handleForgetPassword(line);
        }
        else if(LoginCommands.ANSWER_QUESTION_REGEX.matches(line)){
            handleAnswerQuestion(line);
        }
        else if(LoginCommands.ENTER_MENU_REGEX.matches(line)) {
            handleEnterMenu(line);
        }
        else if(LoginCommands.CURRENT_MENU_REGEX.matches(line)) {
            Result result = controller.showCurrentMenu();
            System.out.println(result.message());
        }
        else invalidCommand();
    }
    public void handleLogin(String input){
        Matcher matcher = LoginCommands.LOGIN_REGEX.getMatcher(input);
        String username= matcher.group("username").trim();
        String password = matcher.group("password").trim();
        boolean stayLoggedIn = matcher.group("stay") != null;
        Result result = controller.login(username,password,stayLoggedIn);
        System.out.println(result.message());
    }
    public void handleForgetPassword(String input){
        Matcher matcher = LoginCommands.FORGET_PASSWORD_REGEX.getMatcher(input);
        String username = matcher.group(1).trim();
        String email = matcher.group(2).trim();
        Result result = controller.forgetPassword(username,email);
        System.out.println(result.message());
    }
    public void handleAnswerQuestion(String input){
        Matcher matcher = LoginCommands.ANSWER_QUESTION_REGEX.getMatcher(input);
        String answer = matcher.group(1).trim();
        Result result = controller.answerQuestion(answer);
        System.out.println(result.message());
    }
    public void handleEnterMenu(String input){
        Matcher matcher = LoginCommands.ENTER_MENU_REGEX.getMatcher(input);
        String menuName = matcher.group("menuName");
        Result result = controller.enterMenu(menuName);
        System.out.println(result.message());
    }
}
