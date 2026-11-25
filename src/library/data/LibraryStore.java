package library.data;

import library.model.*;
import java.util.*;

/** Shared in-memory data access layer. Data lasts for one application session. */
public final class LibraryStore {
    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, BookCopy> copies = new LinkedHashMap<>();
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Loan> loans = new LinkedHashMap<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private int copySequence;
    private int loanSequence;
    private int reservationSequence;

    public void addUser(User user) {
        if (users.putIfAbsent(user.getId(), user) != null) throw new IllegalArgumentException("Duplicate user ID.");
    }
    public void addBook(Book book) {
        if (books.putIfAbsent(book.getIsbn(), book) != null) throw new IllegalArgumentException("ISBN already exists.");
    }
    public Book book(String isbn) { return require(books.get(isbn), "Book not found."); }
    public BookCopy copy(String id) { return require(copies.get(id), "Copy not found."); }
    public User user(String id) { return require(users.get(id), "User not found."); }
    public Loan loan(String id) { return require(loans.get(id), "Loan not found."); }
    public List<Book> books() { return List.copyOf(books.values()); }
    public List<BookCopy> copies() { return List.copyOf(copies.values()); }
    public List<Loan> loans() { return List.copyOf(loans.values()); }
    public List<Reservation> reservations() { return List.copyOf(reservations); }
    public BookCopy addCopy(Book book) {
        BookCopy copy = new BookCopy("C" + (++copySequence), book);
        copies.put(copy.getId(), copy);
        return copy;
    }
    public String nextLoanId() { return "L" + (++loanSequence); }
    public void addLoan(Loan loan) { loans.put(loan.getId(), loan); }
    public Reservation addReservation(Book book, Member member) {
        Reservation reservation = new Reservation("R" + (++reservationSequence), book, member);
        reservations.add(reservation);
        return reservation;
    }
    private static <T> T require(T value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }
}
