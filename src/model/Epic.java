package model;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    protected ArrayList<Subtask> subtaskArray;

    public Epic(String name, String dsc, ArrayList<Subtask> subtaskArray) {
        super(name, dsc, TaskStatus.NEW);
        this.subtaskArray = new ArrayList<Subtask>();
    }

    public Epic(String name, String dsc) {
        super(name, dsc, TaskStatus.NEW);
    }

    public TaskStatus getStatus() {
        return super.getStatus();
    }

    public void setStatus(TaskStatus status) {
        super.setStatus(status);
    }

    public ArrayList<Subtask> getSubtaskArray() {
        return subtaskArray;
    }

    public void setSubtaskArray(ArrayList<Subtask> subtaskArray) {
        this.subtaskArray  = subtaskArray;
    }

    @Override
    public String toString() {
        return "\nЗадача №" + id + ". " + name + '\n' + dsc + '\n' + "Статус задачи: " + status + '\n' + "Подзадачи: ";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Epic epic = (Epic) obj;
        return Objects.equals(name, epic.name)
                && Objects.equals(dsc, epic.dsc)
                && Objects.equals(status, epic.status)
                && Objects.equals(id, epic.id)
                && Objects.equals(subtaskArray, epic.subtaskArray);
    }
}