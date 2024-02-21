public class Subtask extends Task {
    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    private int parentId;
    public Subtask(int id, String name, String description, Status status, Type type, int parentId) {
        super(id, name, description, status, type);
        this.parentId = parentId;
    }

    public Subtask( String name, String description, Status status, Type type, int parentId) {
        super(name, description, status, type);
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "Subtask{" + super.toString() +
                "parentId=" + parentId +
                "} ";
    }
}
