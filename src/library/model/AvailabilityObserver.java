package library.model;

/** Person 3: Observer contract for reservation notifications. */
public interface AvailabilityObserver {
    void onAvailable(BookCopy copy);
}
