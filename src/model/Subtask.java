package model;

public class Subtask extends Task {
    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    private int parentId;
    public Subtask(int id, String name, String description, Status status, int parentId) {
        super(id, name, description, status);
        this.parentId = parentId;
    }

    public Subtask( String name, String description, Status status, int parentId) {
        super(name, description, status);
        this.parentId = parentId;
    }

    public Type getType() {
        return Type.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" + super.toString() +
                "parentId=" + parentId +
                "} ";
    }
}
