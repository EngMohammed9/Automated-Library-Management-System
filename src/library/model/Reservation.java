package library.model;

public final class Reservation {
    public enum Status { WAITING, READY, FULFILLED }
    private final String id;
    private final Book book;
    private final Member member;
    private Status status = Status.WAITING;
    private String copyId;
    public Reservation(String id, Book book, Member member) {
        this.id = id;
        this.book = book;
        this.member = member;
    }
    public String getId() { return id; }
    public Book getBook() { return book; }
    public Member getMember() { return member; }
    public Status getStatus() { return status; }
    public String getCopyId() { return copyId; }
    public void ready(String copyId) {
        if (status != Status.WAITING) throw new IllegalStateException("Reservation is not waiting.");
        this.copyId = copyId;
        status = Status.READY;
    }
    public void fulfill() {
        if (status != Status.READY) throw new IllegalStateException("Reservation is not ready.");
        status = Status.FULFILLED;
    }
    @Override public String toString() {
        return id + " | " + book.getTitle() + " | " + member.getId() + " | " + status
            + (copyId == null ? "" : " | copy=" + copyId);
    }
}
