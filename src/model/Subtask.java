package model;

import java.util.Objects;

public class Subtask extends Task {
    protected Integer epicId;

    public Subtask(String name, String dsc, String status, int epicId) {
        super(name, dsc, status);
        this.epicId = epicId;
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
        return "Подзадача №" + id + ". " + name + '\n' + dsc + '\n' + "Статус задачи: " + status;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            Subtask subtask = (Subtask) obj;
            return Objects.equals(epicId, subtask.epicId);
        }
        return false;
    }
}
