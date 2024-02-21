

public class Epic extends Task {
    public Epic(String name, String description, Status status, Type type) {
        super(name, description, status, type);
    }
    public Epic(int id, String name, String description, Status status, Type type) {
        super(id, name, description, status, type);
    }

    @Override
    public String toString() {
        return "Epic{" + super.toString() + "} ";
    }
}
