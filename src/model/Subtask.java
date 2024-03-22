package model;

public class Subtask extends Task {
    public Epic getParent() {
        return parent;
    }

    private Epic parent;
    public Subtask(int id, String name, String description, Status status, Epic parent) {
        super(id, name, description, status);
        this.parent = parent;
    }

    public Subtask( String name, String description, Epic parent) {
        super(name, description);
        this.parent = parent;
    }

    public Type getType() {
        return Type.SUBTASK;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Subtask subtask = (Subtask) object;
        return getId() == subtask.getId();
    }

    @Override
    public String toString() {
        return "Subtask{" + super.toString() +
                "parentId=" + parent.getId() +
                "} ";
    }
}
