package model;

public class Epic extends Task {
    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }
    public Epic(int id, String name, String description, Status status, Type type) {
        super(id, name, description, status);
    }

    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public String toString() {
        return "Epic{" + super.toString() + "} ";
    }
}
