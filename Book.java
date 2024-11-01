public class Book {
    private String title;
    private String author;
    private String genre;
    private String publisher;
    private int yearPublished;
    private int copiesAvailable;

    public Book(String title, String author, String genre, String publisher, int yearPublished, int copiesAvailable) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publisher = publisher;
        this.yearPublished = yearPublished;
        this.copiesAvailable = copiesAvailable;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public String getPublisher() {
        return publisher;
    }

    public int getYearPublished() {
        return yearPublished;
    }

    public int getCopiesAvailable() {
        return copiesAvailable;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }

    public void setCopiesAvailable(int copiesAvailable) {
        this.copiesAvailable = copiesAvailable;
    }

    
}
