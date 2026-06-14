package pvz.view;

public class SettingMenu implements AppMenu {

    public void showDifficultyChanged(int level) {
        System.out.println("Difficulty changed to " + level + ".");
    }

    public void showInvalidDifficulty() {
        System.out.println("Error: Difficulty level must be between 1 and 5.");
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }
}
