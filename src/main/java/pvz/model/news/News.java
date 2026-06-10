package pvz.model.news;


public class News {
    private String news;
    private boolean isread;

    public News(String news, boolean isread) {
        this.news = news;
        this.isread = isread;
    }

    public String getNews() {
        return news;
    }

    public void setNews(String news) {
        this.news = news;
    }

    public boolean isIsread() {
        return isread;
    }

    public void setIsread(boolean isread) {
        this.isread = isread;
    }
}
