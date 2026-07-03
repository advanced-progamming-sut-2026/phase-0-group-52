package view;

public class ChapterMenu implements AppMenu {

    public void showEnteredChapter(String chapterName) {
        System.out.println("Entered chapter: " + chapterName);
    }

    public void showCoinWallet(int coins) {
        System.out.println("Coin Wallet: " + coins + " coins");
    }

    public void showGemWallet(int gems) {
        System.out.println("Gem Wallet: " + gems + " gems");
    }

    public void showCheatResult(int amount, String type, int newBalance) {
        System.out.println("Added " + amount + " " + type + "(s). New balance: " + newBalance);
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }
}
