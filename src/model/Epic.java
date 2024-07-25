package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Epic extends Task {
    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int id, String name, String description) {
        super(id, name, description);
    }

    public Epic(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        super(id, name, description, status, startTime, duration);
    }

    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Epic epic = (Epic) object;
        return getId() == epic.getId();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
