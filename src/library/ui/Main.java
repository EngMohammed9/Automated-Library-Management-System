package library.ui;

import library.model.*;
import library.service.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Presentation layer: console input/output; business rules live in services. */
public final class Main {
    private final Scanner input = new Scanner(System.in);
    private final LibraryFacade library = LibraryFacade.sample();
    private boolean running = true;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--demo")) { Demo.run(); return; }
        new Main().run();
    }
    private void run() {
        System.out.println("AUTOMATED LIBRARY MANAGEMENT SYSTEM");
        System.out.println("Demo accounts: A1 / admin123; M1, M2, M3 / member123");
        System.out.println("Data is stored in memory for this session. Enter 0 at login to exit.");
        while (running) {
            try {
                String id = ask("User ID: ");
                if (id.equals("0")) break;
                User user = library.login(id, ask("Password: "));
                System.out.println("Welcome, " + user.getName() + ".");
                if (user instanceof Member) printRows(library.notifications());
                boolean signedIn = true;
                while (running && signedIn) {
                    try {
                        signedIn = user instanceof Librarian ? librarianMenu() : memberMenu();
                    } catch (DateTimeParseException e) {
                        System.out.println("Error: use a valid date in YYYY-MM-DD format.");
                    } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                library.logout();
            } catch (IllegalArgumentException | SecurityException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (EndOfInput e) {
                running = false;
            }
        }
        System.out.println("Goodbye.");
    }
    private boolean memberMenu() {
        System.out.println("\n1 Search / list books\n2 Borrow copy\n3 Return copy\n4 Reserve book\n5 My loans\n6 My reservations\n7 Notifications\n0 Log out");
        switch (ask("Choice: ")) {
            case "1" -> search();
            case "2" -> System.out.println(library.borrow(ask("Copy ID: "), LocalDate.now()));
            case "3" -> {
                String loanId = ask("Loan ID: ");
                LocalDate date = date("Return date (YYYY-MM-DD, blank=today; future allowed for demo): ");
                System.out.println(library.returnBook(loanId, date));
            }
            case "4" -> System.out.println(library.reserve(ask("ISBN: ")));
            case "5" -> printRows(library.myLoans());
            case "6" -> printRows(library.myReservations());
            case "7" -> printRows(library.notifications());
            case "0" -> { return false; }
            default -> System.out.println("Invalid menu option.");
        }
        return true;
    }
    private boolean librarianMenu() {
        System.out.println("\n1 Search / list books\n2 Add book\n3 Add copy\n4 Update book\n5 Active loans report\n6 Overdue loans report\n7 Late fees report\n0 Log out");
        switch (ask("Choice: ")) {
            case "1" -> search();
            case "2" -> System.out.println(library.addBook(ask("ISBN: "), ask("Title: "), ask("Author: ")));
            case "3" -> System.out.println(library.addCopy(ask("ISBN: ")));
            case "4" -> {
                library.updateBook(ask("ISBN: "), ask("New title: "), ask("New author: "));
                System.out.println("Book updated.");
            }
            case "5" -> System.out.println(library.report(Reports.Type.ACTIVE, date("Report date (blank=today): ")));
            case "6" -> System.out.println(library.report(Reports.Type.OVERDUE, date("Report date (blank=today): ")));
            case "7" -> System.out.println(library.report(Reports.Type.FINES, date("Report date (blank=today): ")));
            case "0" -> { return false; }
            default -> System.out.println("Invalid menu option.");
        }
        return true;
    }
    private void search() {
        List<Book> books = library.search(ask("Title, author, ISBN (blank=all): "));
        if (books.isEmpty()) System.out.println("No matching books.");
        for (Book book : books) {
            System.out.println(book);
            for (BookCopy copy : library.copies(book.getIsbn())) System.out.println("  " + copy);
        }
    }
    private LocalDate date(String prompt) {
        String text = ask(prompt);
        return text.isEmpty() ? LocalDate.now() : LocalDate.parse(text);
    }
    private String ask(String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) throw new EndOfInput();
        return input.nextLine().trim();
    }
    private static void printRows(List<?> rows) {
        if (rows.isEmpty()) System.out.println("No records.");
        else rows.forEach(System.out::println);
    }
    private static final class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
