package library.service;

import library.data.LibraryStore;
import library.model.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

/** Person 2: borrowing rules and returns. */
public final class LoanService {
    public static final int MAX_LOANS = 3;
    public static final int LOAN_DAYS = 14;
    private final LibraryStore store;
    private final ReservationService reservations;
    private final FineCalculationStrategy fines;
    public LoanService(LibraryStore store, ReservationService reservations, FineCalculationStrategy fines) {
        this.store = store;
        this.reservations = reservations;
        this.fines = fines;
    }
    public Loan borrow(Member member, String copyId, LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Borrowing date is required.");
        if (forMember(member).stream().filter(Loan::isActive).count() >= MAX_LOANS)
            throw new IllegalStateException("Borrowing limit reached (3 active loans).");
        BookCopy copy = store.copy(copyId);
        if (!copy.canBorrow(member.getId())) throw new IllegalStateException("Copy is unavailable or held for another member.");
        boolean sameBook = forMember(member).stream().anyMatch(l -> l.isActive() && l.getCopy().getBook() == copy.getBook());
        if (sameBook) throw new IllegalStateException("You already have a copy of this book on loan.");
        Loan loan = new Loan(store.nextLoanId(), member, copy, date, date.plusDays(LOAN_DAYS));
        copy.borrow(member.getId());
        store.addLoan(loan);
        reservations.fulfill(member, copy);
        return loan;
    }
    public Loan returnBook(Member member, String loanId, LocalDate date) {
        Loan loan = store.loan(loanId);
        if (loan.getMember() != member) throw new SecurityException("This loan belongs to another member.");
        if (!loan.isActive()) throw new IllegalStateException("Loan is already closed.");
        if (date == null || date.isBefore(loan.getBorrowedOn()))
            throw new IllegalArgumentException("Return date cannot precede borrowing date.");
        BigDecimal fine = fines.calculate(loan.getDueOn(), date);
        loan.close(date, fine);
        loan.getCopy().checkIn();
        reservations.allocate(loan.getCopy());
        return loan;
    }
    public List<Loan> forMember(Member member) {
        return store.loans().stream().filter(l -> l.getMember() == member).toList();
    }
}
