package model;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    ArrayList<Subtask> subtaskArray;

    public Epic(String name, String dsc, String status, ArrayList<Subtask> subtaskArray) {
        super(name, dsc, status);
        this.subtaskArray = subtaskArray;
    }

    public Epic(String name, String dsc, ArrayList<Subtask> subtaskArray) {
        super(name, dsc);
        this.subtaskArray = subtaskArray;
    }

    public Epic(String name, String dsc) {
        super(name, dsc);
    }

    public String getStatus() {
        return super.getStatus();
    }

    public void setStatus(String status) {
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
        if (super.equals(obj)) {
            Epic epic = (Epic) obj;
            return Objects.equals(subtaskArray, epic.subtaskArray);
        }
        return false;
    }
}