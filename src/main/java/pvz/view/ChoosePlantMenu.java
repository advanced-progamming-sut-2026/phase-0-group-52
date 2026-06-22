package pvz.view;

public class ChoosePlantMenu implements AppMenu{
    public void showEnteredChapter(String chapterName) {
        System.out.println("Entered chapter: " + chapterName);
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }

}
