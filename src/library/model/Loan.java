package library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Person 2: overdue is derived from the loan date, not the copy state. */
public final class Loan {
    private final String id;
    private final Member member;
    private final BookCopy copy;
    private final LocalDate borrowedOn;
    private final LocalDate dueOn;
    private LocalDate returnedOn;
    private BigDecimal fine = new BigDecimal("0.00");

    public Loan(String id, Member member, BookCopy copy, LocalDate borrowedOn, LocalDate dueOn) {
        if (id == null || member == null || copy == null || borrowedOn == null || dueOn == null
                || dueOn.isBefore(borrowedOn)) throw new IllegalArgumentException("Invalid loan details.");
        this.id = id;
        this.member = member;
        this.copy = copy;
        this.borrowedOn = borrowedOn;
        this.dueOn = dueOn;
    }
    public String getId() { return id; }
    public Member getMember() { return member; }
    public BookCopy getCopy() { return copy; }
    public LocalDate getBorrowedOn() { return borrowedOn; }
    public LocalDate getDueOn() { return dueOn; }
    public LocalDate getReturnedOn() { return returnedOn; }
    public BigDecimal getFine() { return fine; }
    public boolean isActive() { return returnedOn == null; }
    public boolean isOverdue(LocalDate date) { return isActive() && date.isAfter(dueOn); }
    public void close(LocalDate date, BigDecimal fee) {
        if (!isActive()) throw new IllegalStateException("Loan is already closed.");
        if (date == null || date.isBefore(borrowedOn) || fee == null || fee.signum() < 0)
            throw new IllegalArgumentException("Invalid return date or fine.");
        returnedOn = date;
        fine = fee;
    }
    @Override public String toString() {
        return id + " | member=" + member.getId() + " | copy=" + copy.getId()
            + " | borrowed=" + borrowedOn + " | due=" + dueOn
            + (isActive() ? " | ACTIVE" : " | returned=" + returnedOn + " | fine=SAR " + fine);
    }
}
