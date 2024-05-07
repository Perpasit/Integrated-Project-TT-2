package sit.int221.intregratedapi.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    NO_STATUS,
    TO_DO,
    DOING,
    DONE;

    @JsonCreator
    public static TaskStatus fromString(String value) {
        if (value == null || value.isEmpty()) {
            return NO_STATUS;
        }
        return TaskStatus.valueOf(value);
    }
}