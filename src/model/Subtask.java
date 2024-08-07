package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    //public Epic getParent() {
       // return parent;
    //}

    //private Epic parent;
    private int parentId;

    public int getParentId() {
        return parentId;
    }

    public Subtask(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration, int parentId) {
        super(id, name, description, status, startTime, duration);
        this.parentId = parentId;
    }

    public Subtask(String name, String description, LocalDateTime startTime, Duration duration, int parentId) {
        super(name, description, startTime, duration);
        this.parentId = parentId;
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
        return super.toString() +
                parentId;
    }
}
