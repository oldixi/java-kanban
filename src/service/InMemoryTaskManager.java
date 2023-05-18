package service;

import model.*;
import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> taskMap;
    private HashMap<Integer, Epic> epicMap;
    private HashMap<Integer, Subtask> subtaskMap;

    public HistoryManager historyManager = Managers.getDefaultHistory();

    private int tasksId;

    public InMemoryTaskManager() {
        taskMap = new HashMap<>();
        epicMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        tasksId = 0;
    }

    private Integer nextId() {
        return ++tasksId;
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        if (epicMap.containsKey(epicId)) {
            return epicMap.get(epicId).getSubtaskArray();
        }
        return null;
    }

    @Override
    public Task getTask(int taskId) {
        Task task = taskMap.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        Subtask sabtask = subtaskMap.get(subtaskId);
        historyManager.add(sabtask);
        return sabtask;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = epicMap.get(epicId);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public int addNewTask(Task task) {
        int taskId = nextId();
        task.setId(taskId);
        taskMap.put(taskId, task);
        return taskId;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int taskId = nextId();
        epic.setId(taskId);
        epicMap.put(taskId, epic);
        return taskId;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int taskId = nextId();
        subtask.setId(taskId);
        subtaskMap.put(taskId, subtask);
        Epic epicOfSubtask = epicMap.get(subtask.getEpicId());
        if (epicOfSubtask.getStatus() != subtask.getStatus()) {
            epicOfSubtask.setStatus(calcEpicStatus(epicOfSubtask));
            epicMap.replace(subtask.getEpicId(), epicOfSubtask);
        }
        return taskId;
    }

    @Override
    public void updateTask(Task task) {
        int taskId = task.getId();
        if (getTask(task.getId()) != null) {
            taskMap.replace(taskId, task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        Epic epicFromMap = epicMap.get(epicId);
        if (epicFromMap != null) {
            if (epic.getStatus() == epicFromMap.getStatus()) {
                epicMap.replace(epicId, epic);
            }
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        int epicId = subtask.getEpicId();
        Subtask subtaskFromMap = getSubtask(subtaskId);
        Epic epicOfSubtask = epicMap.get(epicId);
        if (subtaskFromMap != null) {
            subtaskMap.replace(subtaskId, subtask);
            boolean isNeedEpicChange = false;
            if (!epicOfSubtask.getSubtaskArray().contains(subtask)) {
                epicOfSubtask.getSubtaskArray().add(subtask);
                isNeedEpicChange = true;
            }
            if (subtask.getStatus() != epicOfSubtask.getStatus()) {
                epicOfSubtask.setStatus(calcEpicStatus(epicOfSubtask));
                isNeedEpicChange = true;
            }
            if (isNeedEpicChange) {
                epicMap.replace(epicId, epicOfSubtask);
            }
        }
    }

    protected Task.TaskStatus calcEpicStatus(Epic epic) {
        Task.TaskStatus status = Task.TaskStatus.IN_PROGRESS;
        boolean isOnlyNew = true;
        boolean isOnlyDone = true;

        if (epic.getSubtaskArray() == null || epic.getSubtaskArray().size() == 0) {
            return Task.TaskStatus.NEW;
        } else {
            for (Subtask subtask : epic.getSubtaskArray()) {
                if (subtask.getStatus() != null) {
                    switch(subtask.getStatus()) {
                        case NEW:
                            isOnlyDone = false;
                            break;
                        case DONE:
                            isOnlyNew = false;
                            break;
                        default:
                            return status;
                    }
                }
            }
        }
        if (isOnlyNew) {
            status = Task.TaskStatus.NEW;;
        } else if (isOnlyDone) {
            status = Task.TaskStatus.DONE;;
        }
        return status;
    }

    @Override
    public void deleteTask(int taskId) {
        taskMap.remove(taskId);
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epicMap.get(epicId);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtaskArray()) {
                subtaskMap.remove(subtask.getId());
            }
            epicMap.remove(epicId);
        }
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask subtask = subtaskMap.get(subtaskId);
        if (subtask != null) {
            subtaskMap.remove(subtaskId);

            int epicId = subtask.getEpicId();
            Epic epicForSubtask = epicMap.get(epicId);
            if (epicForSubtask != null) {
                epicForSubtask.getSubtaskArray().remove(subtask);
                Task.TaskStatus newEpicStatus = calcEpicStatus(epicForSubtask);
                if (!(epicForSubtask.getStatus() == newEpicStatus)) {
                    epicForSubtask.setStatus(newEpicStatus);
                }
                updateEpic(epicForSubtask);
            }
        }
    }

    @Override
    public void deleteTasks() {
        taskMap.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.getSubtaskArray().clear();
            epic.setStatus(Task.TaskStatus.NEW);
        }
        epicMap.forEach((k,v) -> {
            updateEpic(v);
        });
    }

    @Override
    public void deleteEpics() {
        subtaskMap.clear();
        epicMap.clear();
    }
}