package library.ui;

import library.service.*;
import library.model.Loan;
import java.time.LocalDate;

/** Deterministic example output, suitable for capturing submission screenshots. */
public final class Demo {
    private Demo() {}
    public static void run() {
        LibraryFacade library = LibraryFacade.sample();
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = start.plusDays(19);
        System.out.println("=== LIBRARY DEMONSTRATION ===");
        library.login("M1", "member123");
        System.out.println("\n1. Sara searches for Clean Code:");
        library.search("Clean Code").forEach(System.out::println);
        System.out.println("\n2. Sara borrows C1:");
        Loan loan = library.borrow("C1", start);
        System.out.println(loan);
        library.login("M2", "member123");
        System.out.println("\n3. Omar attempts to borrow the same copy:");
        try { library.borrow("C1", start); }
        catch (IllegalStateException e) { System.out.println("Rejected: " + e.getMessage()); }
        System.out.println("\n4. Omar reserves the unavailable book:");
        System.out.println(library.reserve("9780132350884"));
        library.login("A1", "admin123");
        System.out.println("\n5. Librarian views overdue loans on " + end + ":");
        System.out.println(library.report(Reports.Type.OVERDUE, end));
        library.login("M1", "member123");
        System.out.println("\n6. Sara returns C1 five days late:");
        System.out.println(library.returnBook(loan.getId(), end));
        library.copies("9780132350884").forEach(System.out::println);
        library.login("M2", "member123");
        System.out.println("\n7. Observer notification for Omar:");
        library.notifications().forEach(System.out::println);
        System.out.println("\n8. Omar borrows his reserved copy:");
        System.out.println(library.borrow("C1", end));
        library.myReservations().forEach(System.out::println);
        library.login("A1", "admin123");
        System.out.println("\n9. Librarian reports:");
        System.out.println(library.report(Reports.Type.ACTIVE, end));
        System.out.println(library.report(Reports.Type.FINES, end));
    }
}
