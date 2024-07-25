package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    public Epic getParent() {
        return parent;
    }

    private Epic parent;

    public Subtask(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration, Epic parent) {
        super(id, name, description, status, startTime, duration);
        this.parent = parent;
    }

    public Subtask(String name, String description, LocalDateTime startTime, Duration duration, Epic parent) {
        super(name, description, startTime, duration);
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
        return super.toString() +
                parent.getId();
    }
}
