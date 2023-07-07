package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class Epic extends Task {
    protected ArrayList<Subtask> subtaskArray;
    private LocalDateTime endTime;

    public Epic(String name, String dsc, ArrayList<Subtask> subtaskArray) {
        super(name, dsc, TaskStatus.NEW);
        this.subtaskArray = subtaskArray;
        startTime = null;
        endTime = null;
        duration = 0;
    }

    public Epic(String name, String dsc) {
        super(name, dsc, TaskStatus.NEW);
        subtaskArray = new ArrayList<>();
        startTime = null;
        endTime = null;
        duration = 0;
    }

    public TaskStatus getStatus() {
        return super.getStatus();
    }

    public void setStatus(TaskStatus status) {
        super.setStatus(status);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ArrayList<Subtask> getSubtaskArray() {
        return subtaskArray;
    }

    public void setSubtaskArray(ArrayList<Subtask> subtaskArray) {
        this.subtaskArray  = subtaskArray;
    }

    @Override
    public String toString() {
        String subtaskStr = "Нет подзадач";
        if (!subtaskArray.isEmpty()) {
            subtaskStr = String.valueOf(subtaskArray.stream()
                    .map(v -> v.getId())
                    .collect(Collectors.toList()));
        }
        return "\nЗадача №" + id + ". " + name + '\n' + dsc + '\n' + "Статус задачи: "
                + status + '\n' + "Подзадачи: " + subtaskStr;
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

    @Override
    public Epic clone() {
        Epic epicClone = new Epic(new String(name), new String(dsc), new ArrayList<>(subtaskArray));
        epicClone.setId(getId());
        epicClone.setStatus(getStatus());
        return epicClone;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}