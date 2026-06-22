package pvz.view;

public class MainMenu implements AppMenu {

    public void showLoggedOut() {
        System.out.println("Logged out successfully.");
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }
}
