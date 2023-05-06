package service;

import java.util.ArrayList;
import model.*;

public class ManagerForInterface extends Manager {

    public ArrayList<Task> getTasks() {
        return getAllTasks();
    }

    public ArrayList<Subtask> getSubtasks() {
        return getAllSubtasks();
    }

    public ArrayList<Epic> getEpics() {
        return getAllEpics();
    }

    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        return getAllSubtasksForEpic(epicId);
    }

    public Task getTask(int taskId) {
        return getTask(taskId, Manager.TASK);
    }

    public Subtask getSubtask(int subtaskId) {
        return (Subtask) getTask(subtaskId, Manager.SUBTASK);
    }

    public Epic getEpic(int epicId) {
        return (Epic) getTask(epicId, Manager.EPIC);
    }


    public int addNewTask(Task task) {
        return createTask(task, Manager.TASK);
    }

    public int addNewEpic(Epic epic) {
        return createTask(epic, Manager.EPIC);
    }

    public int addNewSubtask(Subtask subtask) {
        return createTask(subtask, Manager.SUBTASK);
    }

    public void updateTask(Task task) {
        updateTask(task.getId(), task, Manager.TASK);
    }

    public void updateEpic(Epic epic) {
        updateTask(epic.getId(), epic, Manager.EPIC);
    }

    public void updateSubtask(Subtask subtask) {
        updateTask(subtask.getId(), subtask, Manager.SUBTASK);
    }

    public void deleteTask(int taskId) {
        removeTask(taskId, Manager.TASK);
    }

    public void deleteEpic(int epicId) {
        removeTask(epicId, Manager.EPIC);
    }

    public void deleteSubtask(int subtaskId) {
        removeTask(subtaskId, Manager.SUBTASK);
    }

    public void deleteTasks() {
        removeAllTasks(Manager.TASK);
    }

    public void deleteSubtasks() {
        removeAllTasks(Manager.SUBTASK);
    }

    public void deleteEpics() {
        removeAllTasks(Manager.EPIC);
    }

}