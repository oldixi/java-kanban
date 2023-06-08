package service;

import model.*;
import java.util.List;

public interface TaskManager {
     List<Task> getTasks();
     List<Subtask> getSubtasks();
     List<Epic> getEpics();

     List<Subtask> getEpicSubtasks(int epicId);

     Task getTask(int taskId);
     Subtask getSubtask(int subtaskId);
     Epic getEpic(int epicId);

     int addNewTask(Task task);
     int addNewEpic(Epic epic);
     int addNewSubtask(Subtask subtask);

     void updateTask(Task task);
     void updateEpic(Epic epic);
     void updateSubtask(Subtask subtask);

     void deleteTask(int taskId);
     void deleteEpic(int epicId);
     void deleteSubtask(int subtaskId);

     void deleteTasks();
     void deleteSubtasks();
     void deleteEpics();

     HistoryManager getHistoryManager();
}