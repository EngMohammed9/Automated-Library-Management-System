package library.service;

import library.data.LibraryStore;
import library.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Person 2: Facade pattern; presentation uses this entry point for all operations. */
public final class LibraryFacade {
    private final LibraryStore store;
    private final CatalogService catalog;
    private final ReservationService reservations;
    private final LoanService loans;
    private final Map<String, String> passwords;
    private User current;

    public LibraryFacade(LibraryStore store, Map<String, String> passwords, FineCalculationStrategy fines) {
        this.store = store;
        this.passwords = Map.copyOf(passwords);
        catalog = new CatalogService(store);
        reservations = new ReservationService(store);
        loans = new LoanService(store, reservations, fines);
    }
    public static LibraryFacade sample() {
        LibraryStore store = new LibraryStore();
        store.addUser(new Librarian("A1", "Librarian"));
        store.addUser(new Member("M1", "Sara"));
        store.addUser(new Member("M2", "Omar"));
        store.addUser(new Member("M3", "Lina"));
        CatalogService catalog = new CatalogService(store);
        catalog.addBook("9780132350884", "Clean Code", "Robert C. Martin");
        catalog.addBook("9780201633610", "Design Patterns", "Erich Gamma et al.");
        catalog.addBook("9780135957059", "The Pragmatic Programmer", "David Thomas and Andrew Hunt");
        catalog.addBook("9780134685991", "Effective Java", "Joshua Bloch");
        for (Book book : store.books()) catalog.addCopy(book.getIsbn());
        return new LibraryFacade(store, Map.of("A1", "admin123", "M1", "member123", "M2", "member123", "M3", "member123"),
            new DailyFineStrategy(new BigDecimal("1.00")));
    }
    public User login(String id, String password) {
        current = null;
        if (password == null || !password.equals(passwords.get(id))) throw new SecurityException("Invalid ID or password.");
        current = store.user(id);
        return current;
    }
    public void logout() { current = null; }
    public User currentUser() { requireLogin(); return current; }
    public List<Book> search(String query) { requireLogin(); return catalog.search(query); }
    public List<BookCopy> copies(String isbn) { requireLogin(); return catalog.copies(isbn); }
    public Book addBook(String isbn, String title, String author) { requireLibrarian(); return catalog.addBook(isbn, title, author); }
    public void updateBook(String isbn, String title, String author) { requireLibrarian(); catalog.updateBook(isbn, title, author); }
    public BookCopy addCopy(String isbn) {
        requireLibrarian();
        BookCopy copy = catalog.addCopy(isbn);
        reservations.allocate(copy);
        return copy;
    }
    public Loan borrow(String copyId, LocalDate date) { return loans.borrow(requireMember(), copyId, date); }
    public Loan returnBook(String loanId, LocalDate date) { return loans.returnBook(requireMember(), loanId, date); }
    public Reservation reserve(String isbn) { return reservations.reserve(requireMember(), isbn); }
    public List<Loan> myLoans() { return loans.forMember(requireMember()); }
    public List<Reservation> myReservations() { return reservations.forMember(requireMember()); }
    public List<String> notifications() { return requireMember().getNotifications(); }
    public String report(Reports.Type type, LocalDate date) {
        requireLibrarian();
        if (date == null) throw new IllegalArgumentException("Report date is required.");
        return Reports.creator(type).generate(store, date);
    }
    private void requireLogin() { if (current == null) throw new SecurityException("Please log in first."); }
    private Member requireMember() {
        requireLogin();
        if (!(current instanceof Member)) throw new SecurityException("Member access required.");
        return (Member) current;
    }
    private void requireLibrarian() {
        requireLogin();
        if (!(current instanceof Librarian)) throw new SecurityException("Librarian access required.");
    }
}
