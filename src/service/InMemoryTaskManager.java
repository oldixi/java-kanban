package service;

import model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskMap;
    private final Map<Integer, Epic> epicMap;
    private final Map<Integer, Subtask> subtaskMap;

    private final HistoryManager historyManager;

    private int tasksId = 0;

    public InMemoryTaskManager() {
        taskMap = new HashMap<>();
        epicMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
        tasksId = 0;
    }

    private Integer nextId() {
        return ++tasksId;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
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

    private int setId(Task task) {
        int taskId = task.getId();
        if (taskId == 0) {
            taskId = nextId();
            task.setId(taskId);
        }
        if (taskId > tasksId) {
            tasksId = taskId;
        }
        return taskId;
    }

    @Override
    public int addNewTask(Task task) {
        int taskId = setId(task);
        taskMap.put(taskId, task);
        return taskId;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int epicId = setId(epic);
        epicMap.put(epicId, epic);
        return epicId;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int subtaskId = setId(subtask);
        subtaskMap.put(subtaskId, subtask);

        Epic epicOfSubtask = epicMap.get(subtask.getEpicId());
        if (epicOfSubtask != null && epicOfSubtask.getStatus() != subtask.getStatus()) {
            epicOfSubtask.setStatus(calcEpicStatus(epicOfSubtask));
            epicMap.replace(subtask.getEpicId(), epicOfSubtask);
        }
        return subtaskId;
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

    protected TaskStatus calcEpicStatus(Epic epic) {
        TaskStatus status = TaskStatus.IN_PROGRESS;
        boolean isOnlyNew = true;
        boolean isOnlyDone = true;

        if (epic.getSubtaskArray() == null || epic.getSubtaskArray().size() == 0) {
            return TaskStatus.NEW;
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
            status = TaskStatus.NEW;
        } else if (isOnlyDone) {
            status = TaskStatus.DONE;
        }
        return status;
    }

    @Override
    public void deleteTask(int taskId) {
        taskMap.remove(taskId);
        historyManager.remove(taskId);
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epicMap.get(epicId);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtaskArray()) {
                subtaskMap.remove(subtask.getId());
                historyManager.remove(subtask.getId());
            }
            epicMap.remove(epicId);
            historyManager.remove(epicId);
        }
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask subtask = subtaskMap.get(subtaskId);
        if (subtask != null) {
            subtaskMap.remove(subtaskId);
            historyManager.remove(subtaskId);

            int epicId = subtask.getEpicId();
            Epic epicForSubtask = epicMap.get(epicId);
            if (epicForSubtask != null) {
                epicForSubtask.getSubtaskArray().remove(subtask);
                TaskStatus newEpicStatus = calcEpicStatus(epicForSubtask);
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
            epic.setStatus(TaskStatus.NEW);
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