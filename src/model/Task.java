package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task implements Comparable<Task> {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    protected int id;
    protected String name;
    protected String dsc;
    protected TaskStatus status;
    protected int duration = 0;
    protected LocalDateTime startTime = null;

    public Task(String name, String dsc) {
        this.name = name;
        this.dsc = dsc;
        this.status = TaskStatus.NEW;
    }

    public Task(String name, String dsc, TaskStatus status) {
        this(name, dsc);
        this.status = status;
    }

    public Task(String name, String dsc, TaskStatus status, int duration, LocalDateTime startTime) {
        this(name, dsc);
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task() {
        this.status = TaskStatus.NEW;
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
        String startDateStr = "";
        if (getStartTime() != null) {
            startDateStr = "Дата с: "
                    + getStartTime().format(FORMATTER);
        }
        return "\nЗадача №" + id + ". " + name + '\n' + dsc + '\n'
                + "Статус задачи: " + status + '\n' + startDateStr;
    }

    @Override
    public Task clone() {
        Task taskClone = new Task(new String(name), new String(dsc), getStatus());
        taskClone.setId(getId());
        return taskClone;
    }

    @Override
    public int compareTo(Task task2) {
        if (getStartTime() == null &&  task2.getStartTime() == null) {
            return 0;
        } else if (getStartTime() == null) {
            return 1;
        } else if (task2.getStartTime() == null) {
            return -1;
        }
        return getStartTime().compareTo(task2.getStartTime());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDsc() {
        return dsc;
    }

    public void setDsc(String dsc) {
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        LocalDateTime endTime = null;
        if (startTime != null) {
            endTime = startTime.plusMinutes(duration);
        }
        return endTime;
    }
}