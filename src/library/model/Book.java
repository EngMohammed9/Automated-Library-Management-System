package library.model;

/** Person 1: bibliographic information shared by physical copies. */
public final class Book {
    private final String isbn;
    private String title;
    private String author;

    public Book(String isbn, String title, String author) {
        this.isbn = required(isbn);
        update(title, author);
    }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public void update(String title, String author) {
        String validTitle = required(title);
        String validAuthor = required(author);
        this.title = validTitle;
        this.author = validAuthor;
    }
    private static String required(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Book fields cannot be blank.");
        return value.trim();
    }
    @Override public String toString() { return isbn + " | " + title + " | " + author; }
}
