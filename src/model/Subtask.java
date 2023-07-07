package model;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class Subtask extends Task {
    protected Integer epicId;

    public Subtask(String name, String dsc, TaskStatus status, int epicId) {
        super(name, dsc, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String dsc, TaskStatus status) {
        super(name, dsc, status);
    }

    public Subtask(String name, String dsc, int epicId) {
        super(name, dsc);
        this.epicId = epicId;
    }

    public Subtask(String name, String dsc) {
        super(name, dsc);
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        String startDateStr = "";
        if (getStartTime() != null) {
            startDateStr = "Дата с: "
                    + getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        return "\nПодзадача №" + id + ". " + name + '\n' + dsc + '\n' + "Статус задачи: "
                + status + '\n' + startDateStr;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Subtask subtask = (Subtask) obj;
        return Objects.equals(name, subtask.name)
                && Objects.equals(dsc, subtask.dsc)
                && Objects.equals(status, subtask.status)
                && Objects.equals(id, subtask.id)
                && Objects.equals(epicId, subtask.epicId);
    }

    @Override
    public Subtask clone() {
        Subtask subtaskClone = new Subtask(new String(name), new String(dsc), epicId);
        subtaskClone.setId(getId());
        subtaskClone.setStatus(getStatus());
        return subtaskClone;
    }
}
