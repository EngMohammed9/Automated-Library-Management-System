package library.service;

import library.data.LibraryStore;
import library.model.*;
import java.util.List;
import java.util.Locale;

/** Person 1: catalog business logic. */
public final class CatalogService {
    private final LibraryStore store;
    public CatalogService(LibraryStore store) { this.store = store; }
    public Book addBook(String isbn, String title, String author) {
        Book book = new Book(isbn, title, author);
        store.addBook(book);
        return book;
    }
    public void updateBook(String isbn, String title, String author) { store.book(isbn).update(title, author); }
    public BookCopy addCopy(String isbn) { return store.addCopy(store.book(isbn)); }
    public List<Book> search(String query) {
        String term = query.trim().toLowerCase(Locale.ROOT);
        return store.books().stream().filter(b -> b.getIsbn().toLowerCase(Locale.ROOT).contains(term)
            || b.getTitle().toLowerCase(Locale.ROOT).contains(term)
            || b.getAuthor().toLowerCase(Locale.ROOT).contains(term)).toList();
    }
    public List<BookCopy> copies(String isbn) {
        store.book(isbn);
        return store.copies().stream().filter(c -> c.getBook().getIsbn().equals(isbn)).toList();
    }
}
