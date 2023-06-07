package service;

import model.*;
import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    public ArrayList<Task> getTasks();
    public ArrayList<Subtask> getSubtasks();
    public ArrayList<Epic> getEpics();

    public ArrayList<Subtask> getEpicSubtasks(int epicId);

    public Task getTask(int taskId);
    public Subtask getSubtask(int subtaskId);
    public Epic getEpic(int epicId);

    public int addNewTask(Task task);
    public int addNewEpic(Epic epic);
    public int addNewSubtask(Subtask subtask);

    public void updateTask(Task task);
    public void updateEpic(Epic epic);
    public void updateSubtask(Subtask subtask);

    public void deleteTask(int taskId);
    public void deleteEpic(int epicId);
    public void deleteSubtask(int subtaskId);

    public void deleteTasks();
    public void deleteSubtasks();
    public void deleteEpics();

    public HistoryManager getHistoryManager();
}