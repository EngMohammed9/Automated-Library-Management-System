package library.model;

/** Person 1: State pattern. Each state controls the permitted transitions. */
public final class BookCopy {
    public interface CopyState {
        String name();
        void borrow(BookCopy copy, String memberId);
        void checkIn(BookCopy copy);
        void hold(BookCopy copy, String memberId);
    }
    public static final class AvailableState implements CopyState {
        public String name() { return "Available"; }
        public void borrow(BookCopy copy, String memberId) { copy.state = new CheckedOutState(); }
        public void checkIn(BookCopy copy) { throw new IllegalStateException("Copy is already available."); }
        public void hold(BookCopy copy, String memberId) {
            copy.heldFor = memberId;
            copy.state = new ReservedState();
        }
    }
    public static final class CheckedOutState implements CopyState {
        public String name() { return "Checked Out"; }
        public void borrow(BookCopy copy, String memberId) { throw new IllegalStateException("Copy is checked out."); }
        public void checkIn(BookCopy copy) { copy.state = new AvailableState(); }
        public void hold(BookCopy copy, String memberId) { throw new IllegalStateException("Return the copy before holding it."); }
    }
    public static final class ReservedState implements CopyState {
        public String name() { return "Reserved"; }
        public void borrow(BookCopy copy, String memberId) {
            if (!memberId.equals(copy.heldFor)) throw new IllegalStateException("Copy is held for another member.");
            copy.heldFor = null;
            copy.state = new CheckedOutState();
        }
        public void checkIn(BookCopy copy) { throw new IllegalStateException("Reserved copy has no active loan."); }
        public void hold(BookCopy copy, String memberId) { throw new IllegalStateException("Copy is already reserved."); }
    }
    private final String id;
    private final Book book;
    private CopyState state = new AvailableState();
    private String heldFor;

    public BookCopy(String id, Book book) {
        if (id == null || id.isBlank() || book == null) throw new IllegalArgumentException("Copy needs an ID and book.");
        this.id = id;
        this.book = book;
    }
    public String getId() { return id; }
    public Book getBook() { return book; }
    public String getStateName() { return state.name(); }
    public String getHeldFor() { return heldFor; }
    public boolean isAvailable() { return state instanceof AvailableState; }
    public boolean canBorrow(String memberId) {
        return isAvailable() || (state instanceof ReservedState && memberId.equals(heldFor));
    }
    public void borrow(String memberId) {
        if (memberId == null || memberId.isBlank()) throw new IllegalArgumentException("Member ID is required.");
        state.borrow(this, memberId);
    }
    public void checkIn() { state.checkIn(this); }
    public void hold(String memberId) {
        if (memberId == null || memberId.isBlank()) throw new IllegalArgumentException("Member ID is required.");
        state.hold(this, memberId);
    }
    @Override public String toString() {
        return id + " | " + book.getTitle() + " | " + state.name()
            + (heldFor == null ? "" : " (held for " + heldFor + ")");
    }
}
