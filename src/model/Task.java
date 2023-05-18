package model;

import java.util.Objects;

public class Task {
    public enum TaskStatus {
        NEW,
        IN_PROGRESS,
        DONE
    }

    protected int id;
    protected String name;
    protected String dsc;
    protected TaskStatus status;

    public Task(String name, String dsc) {
        this.name = name;
        this.dsc = dsc;
        this.status = TaskStatus.NEW;
    }

    public Task(String name, String dsc, TaskStatus status) {
        this(name, dsc);
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return Objects.equals(name, task.name)
                && Objects.equals(dsc, task.dsc)
                && Objects.equals(status, task.status)
                && Objects.equals(id, task.id);
    }

    @Override
    public String toString() {
        return "\nЗадача №" + id + ". " + name + '\n' + dsc + '\n' + "Статус задачи: " + status;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDsc() {
        return dsc;
    }

    protected void setDsc(String dsc) {
        this.dsc = dsc;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}