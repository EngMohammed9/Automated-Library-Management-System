package library.service;

import library.data.LibraryStore;
import library.model.*;
import java.util.List;

/** Person 3: first-in-first-out queue, also acting as the Observer subject. */
public final class ReservationService {
    private final LibraryStore store;
    public ReservationService(LibraryStore store) { this.store = store; }
    public Reservation reserve(Member member, String isbn) {
        Book book = store.book(isbn);
        boolean available = store.copies().stream().anyMatch(c -> c.getBook() == book && c.isAvailable());
        if (available) throw new IllegalStateException("A copy is available; borrow it instead.");
        boolean duplicate = store.reservations().stream().anyMatch(r -> r.getBook() == book
            && r.getMember() == member && r.getStatus() != Reservation.Status.FULFILLED);
        if (duplicate) throw new IllegalStateException("You already have an active reservation for this book.");
        boolean borrowed = store.loans().stream().anyMatch(l -> l.isActive()
            && l.getMember() == member && l.getCopy().getBook() == book);
        if (borrowed) throw new IllegalStateException("You already have a copy of this book on loan.");
        return store.addReservation(book, member);
    }
    public void allocate(BookCopy copy) {
        if (!copy.isAvailable()) return;
        store.reservations().stream().filter(r -> r.getBook() == copy.getBook()
            && r.getStatus() == Reservation.Status.WAITING).findFirst().ifPresent(r -> {
                copy.hold(r.getMember().getId());
                r.ready(copy.getId());
                notifyObserver(r.getMember(), copy);
            });
    }
    private void notifyObserver(AvailabilityObserver observer, BookCopy copy) { observer.onAvailable(copy); }
    public void fulfill(Member member, BookCopy copy) {
        store.reservations().stream().filter(r -> r.getStatus() == Reservation.Status.READY
            && r.getMember() == member && copy.getId().equals(r.getCopyId()))
            .findFirst().ifPresent(Reservation::fulfill);
    }
    public List<Reservation> forMember(Member member) {
        return store.reservations().stream().filter(r -> r.getMember() == member).toList();
    }
}
