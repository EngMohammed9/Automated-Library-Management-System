package library.model;

import java.util.ArrayList;
import java.util.List;

/** Person 2: member model; Person 3: notification observer implementation. */
public final class Member extends User implements AvailabilityObserver {
    private final List<String> notifications = new ArrayList<>();
    public Member(String id, String name) { super(id, name); }
    @Override public void onAvailable(BookCopy copy) {
        notifications.add("Copy " + copy.getId() + " of '" + copy.getBook().getTitle() + "' is held for you.");
    }
    public List<String> getNotifications() { return List.copyOf(notifications); }
}
