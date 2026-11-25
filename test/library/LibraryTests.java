package library;

import library.service.*;
import library.model.*;
import java.time.LocalDate;
import java.math.BigDecimal;

/** Dependency-free integration checks; run with java -cp out library.LibraryTests. */
public final class LibraryTests {
    private static int checks;
    private static final LocalDate DAY = LocalDate.of(2026, 11, 1);
    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        checks++;
    }
    private static void rejects(Class<? extends RuntimeException> type, Runnable operation) {
        try { operation.run(); }
        catch (RuntimeException e) {
            if (!type.isInstance(e)) throw new AssertionError("Unexpected exception", e);
            checks++;
            return;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
    public static void main(String[] args) {
        LibraryFacade lib = LibraryFacade.sample();
        rejects(SecurityException.class, () -> lib.search(""));
        rejects(SecurityException.class, () -> lib.login("M1", "wrong"));
        lib.login("M1", "member123");
        check(lib.search("CLEAN").size() == 1, "Case-insensitive title search");
        check(lib.search("Joshua").size() == 1, "Author search");
        check(lib.search("9780132350884").size() == 1, "ISBN search");
        rejects(SecurityException.class, () -> lib.addBook("X", "Title", "Author"));
        rejects(SecurityException.class, () -> lib.report(Reports.Type.ACTIVE, DAY));
        Loan first = lib.borrow("C1", DAY);
        check(first.getDueOn().equals(DAY.plusDays(14)), "14-day due date");
        rejects(IllegalStateException.class, () -> lib.borrow("C1", DAY));
        rejects(IllegalStateException.class, () -> lib.reserve("9780132350884"));
        lib.borrow("C2", DAY);
        lib.borrow("C3", DAY);
        rejects(IllegalStateException.class, () -> lib.borrow("C4", DAY));
        check(lib.myLoans().size() == 3, "Failed borrowing leaves loans unchanged");

        lib.login("M2", "member123");
        rejects(SecurityException.class, () -> lib.returnBook(first.getId(), DAY));
        rejects(IllegalStateException.class, () -> lib.reserve("9780134685991"));
        Reservation second = lib.reserve("9780132350884");
        rejects(IllegalStateException.class, () -> lib.reserve("9780132350884"));
        lib.login("M3", "member123");
        Reservation third = lib.reserve("9780132350884");
        lib.login("A1", "admin123");
        check(lib.report(Reports.Type.OVERDUE, DAY.plusDays(14)).contains("No records."), "Due day is not overdue");
        check(lib.report(Reports.Type.OVERDUE, DAY.plusDays(15)).contains(first.getId()), "Active overdue loan reported");
        rejects(SecurityException.class, () -> lib.borrow("C4", DAY));

        lib.login("M1", "member123");
        rejects(IllegalArgumentException.class, () -> lib.returnBook(first.getId(), DAY.minusDays(1)));
        check(first.isActive(), "Invalid return leaves loan active");
        lib.returnBook(first.getId(), DAY.plusDays(19));
        check(first.getFine().compareTo(new BigDecimal("5.00")) == 0, "Five days late costs SAR 5");
        check(second.getStatus() == Reservation.Status.READY, "First reservation becomes ready");
        check(third.getStatus() == Reservation.Status.WAITING, "Second reservation remains waiting");
        check(lib.copies("9780132350884").get(0).getHeldFor().equals("M2"), "Copy held for first waiting member");
        rejects(IllegalStateException.class, () -> lib.returnBook(first.getId(), DAY.plusDays(20)));

        lib.login("M3", "member123");
        rejects(IllegalStateException.class, () -> lib.borrow("C1", DAY.plusDays(19)));
        check(lib.notifications().isEmpty(), "Later member not notified prematurely");
        lib.login("M2", "member123");
        check(lib.notifications().size() == 1, "Observer sends one notification");
        Loan next = lib.borrow("C1", DAY.plusDays(19));
        check(second.getStatus() == Reservation.Status.FULFILLED, "Collecting hold fulfills reservation");
        lib.returnBook(next.getId(), DAY.plusDays(20));
        check(next.getFine().signum() == 0, "Early return has no fine");
        check(third.getStatus() == Reservation.Status.READY, "Next waiting member gets returned copy");

        lib.login("A1", "admin123");
        check(lib.report(Reports.Type.FINES, DAY.plusDays(20)).contains("SAR 5.00"), "Fine report totals recorded fees");
        rejects(IllegalArgumentException.class, () -> lib.addBook("9780132350884", "Duplicate", "Author"));
        rejects(IllegalArgumentException.class, () -> lib.addBook("X", " ", "Author"));
        String original = lib.search("9780132350884").get(0).getTitle();
        rejects(IllegalArgumentException.class, () -> lib.updateBook("9780132350884", "New", ""));
        check(lib.search("9780132350884").get(0).getTitle().equals(original), "Invalid update is atomic");
        lib.updateBook("9780132350884", "Clean Code Revised", "Robert C. Martin");
        check(lib.copies("9780132350884").get(0).getBook().getTitle().equals("Clean Code Revised"), "Copies share updated book record");
        lib.addBook("X", "New Book", "New Author");
        check(lib.addCopy("X").isAvailable(), "Librarian adds available copy");
        lib.logout();
        rejects(SecurityException.class, () -> lib.report(Reports.Type.ACTIVE, DAY));
        lib.login("A1", "admin123");
        rejects(SecurityException.class, () -> lib.login("M1", "bad"));
        rejects(SecurityException.class, () -> lib.addCopy("X"));

        // Adding a new copy must honor waiting reservations, not bypass their queue.
        LibraryFacade extra = LibraryFacade.sample();
        extra.login("M1", "member123");
        extra.borrow("C1", DAY);
        extra.login("M2", "member123");
        Reservation waiting = extra.reserve("9780132350884");
        extra.login("A1", "admin123");
        BookCopy added = extra.addCopy("9780132350884");
        check("M2".equals(added.getHeldFor()), "New copy allocated to queue");
        check(waiting.getStatus() == Reservation.Status.READY, "New copy marks reservation ready");

        FineCalculationStrategy standard = new DailyFineStrategy(new BigDecimal("1.00"));
        check(standard.calculate(DAY, DAY).signum() == 0, "No fee on due date");
        check(new DailyFineStrategy(new BigDecimal("2.00")).calculate(DAY, DAY.plusDays(2))
            .compareTo(new BigDecimal("4.00")) == 0, "Strategy can change fee policy");
        BookCopy standalone = new BookCopy("TEST", new Book("T", "Title", "Author"));
        rejects(IllegalStateException.class, standalone::checkIn);
        standalone.borrow("M1");
        rejects(IllegalStateException.class, () -> standalone.hold("M2"));
        standalone.checkIn();
        check(standalone.isAvailable(), "Unreserved return restores availability");
        System.out.println("PASS: " + checks + " checks.");
    }
}
