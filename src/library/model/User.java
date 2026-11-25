package library.model;

public abstract class User {
    private final String id;
    private final String name;
    protected User(String id, String name) {
        if (id == null || id.isBlank() || name == null || name.isBlank())
            throw new IllegalArgumentException("User ID and name are required.");
        this.id = id;
        this.name = name;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    @Override public String toString() { return id + " | " + name; }
}
