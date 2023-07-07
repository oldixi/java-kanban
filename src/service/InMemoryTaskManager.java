package service;

import model.*;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskMap;
    private final Map<Integer, Epic> epicMap;
    private final Map<Integer, Subtask> subtaskMap;

    private final HistoryManager historyManager;

    private final Set<Task> prioritizedTasksSet = new TreeSet<>();

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
        Subtask subtask = subtaskMap.get(subtaskId);
        historyManager.add(subtask);
        return subtask;
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
        return tasksId;
    }

    private void setStartTimeNullIfCrossed(Task task) {
        if ((task.getStartTime() != null || task.getEndTime() != null)
                && (isCrossed(task.getStartTime(), task.getEndTime()))) {
            task.setStartTime(null);
        }
    }

    @Override
    public int addNewTask(Task task) {
        int taskId = setId(task);
        taskMap.put(taskId, task);
        setStartTimeNullIfCrossed(task);
        prioritizedTasksSet.add(task);
        return taskId;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int epicId = setId(epic);
        epicMap.put(epicId, epic);
        if (epic.getDuration() != 0
                || epic.getStartTime() != null
                    && epic.getStartTime() != LocalDateTime.MIN) {
            epic.setEndTime(epic.getStartTime().plusMinutes(epic.getDuration()));
        }
        return epicId;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int subtaskId = 0;
        Epic epicOfSubtask = epicMap.get(subtask.getEpicId());
        if (epicOfSubtask != null) {
            subtaskId = setId(subtask);
            subtaskMap.put(subtaskId, subtask);
            if (epicOfSubtask.getStatus() != subtask.getStatus()) {
                epicOfSubtask.setStatus(calcEpicStatus(epicOfSubtask));
            }
            if (epicOfSubtask.getDuration() == 0 && subtask.getDuration() != 0
                    || subtask.getStartTime() != null) {
                calcEpicStartAndEndTime(epicOfSubtask);
            }
        }
        setStartTimeNullIfCrossed(subtask);
        prioritizedTasksSet.add(subtask);
        return subtaskId;
    }

    @Override
    public void updateTask(Task task) {
        int taskId = task.getId();
        if (getTask(task.getId()) != null) {
            taskMap.replace(taskId, task);
            prioritizedTasksSet.remove(task);
            prioritizedTasksSet.add(task);
            setStartTimeNullIfCrossed(task);
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
            prioritizedTasksSet.remove(subtask);
            prioritizedTasksSet.add(subtask);
            setStartTimeNullIfCrossed(subtask);
            boolean isNeedEpicChange = false;
            if (!epicOfSubtask.getSubtaskArray().contains(subtask)) {
                epicOfSubtask.getSubtaskArray().add(subtask);
                isNeedEpicChange = true;
            }
            if (subtask.getStatus() != epicOfSubtask.getStatus()) {
                epicOfSubtask.setStatus(calcEpicStatus(epicOfSubtask));
                isNeedEpicChange = true;
            }
            if (epicOfSubtask.getDuration() == 0 && subtask.getDuration() != 0
                    || subtask.getStartTime() != null) {
                calcEpicStartAndEndTime(epicOfSubtask);
                isNeedEpicChange = true;
            }
            if (isNeedEpicChange) {
                epicMap.replace(epicId, epicOfSubtask);
            }
        }
    }

    public TaskStatus calcEpicStatus(Epic epic) {
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

    public void calcEpicStartAndEndTime(Epic epic) {
        if (epic.getSubtaskArray() == null || epic.getSubtaskArray().size() == 0) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(0);
        } else {
            int sumSubtasksDuration = 0;
            LocalDateTime minStartTime = LocalDateTime.MAX;

            for (Subtask subtask : epic.getSubtaskArray()) {
                if (subtask.getStartTime() != null && subtask.getStartTime().isBefore(minStartTime)) {
                    minStartTime = subtask.getStartTime();
                }
                sumSubtasksDuration += subtask.getDuration();
            }
            if (minStartTime != LocalDateTime.MAX) {
                LocalDateTime maxEndTime = minStartTime.plusMinutes(sumSubtasksDuration);
                epic.setStartTime(minStartTime);
                epic.setEndTime(maxEndTime);
            }
            epic.setDuration(sumSubtasksDuration);
        }
    }

    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasksSet;
    }

    public boolean isCrossed(LocalDateTime startTime, LocalDateTime endTime) {
        Optional<Task> crossedFirstTask = null;
        if (startTime != null && endTime != null) {
            crossedFirstTask = getPrioritizedTasks().stream()
                    .filter(v -> (v.getStartTime() != null && v.getEndTime() != null
                            && (startTime.isAfter(v.getStartTime()) && startTime.isBefore(v.getEndTime())
                                || (endTime.isAfter(v.getStartTime()) && endTime.isBefore(v.getEndTime()))
                                || startTime.equals(v.getStartTime()) || startTime.equals(v.getEndTime())
                                || endTime.equals(v.getStartTime()) || endTime.equals(v.getEndTime()))))
                    .findFirst();
        }
        return crossedFirstTask.isPresent();
    }

    @Override
    public void deleteTask(int taskId) {
        Task task = taskMap.get(taskId);
        taskMap.remove(taskId);
        if (task != null) {
            prioritizedTasksSet.remove(task);
        }
        historyManager.remove(taskId);
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epicMap.get(epicId);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtaskArray()) {
                subtaskMap.remove(subtask.getId());
                prioritizedTasksSet.remove(subtask);
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
            prioritizedTasksSet.remove(subtask);
            historyManager.remove(subtaskId);

            int epicId = subtask.getEpicId();
            Epic epicForSubtask = epicMap.get(epicId);
            if (epicForSubtask != null) {
                epicForSubtask.getSubtaskArray().remove(subtask);
                TaskStatus newEpicStatus = calcEpicStatus(epicForSubtask);
                if (!(epicForSubtask.getStatus() == newEpicStatus)) {
                    epicForSubtask.setStatus(newEpicStatus);
                }
                calcEpicStartAndEndTime(epicForSubtask);
                updateEpic(epicForSubtask);
            }
        }
    }

    @Override
    public void deleteTasks() {
        if (getTasks() != null) {
            getTasks().forEach(prioritizedTasksSet::remove);
        }
        taskMap.forEach((k,v) -> historyManager.remove(v.getId()));
        taskMap.clear();
    }

    @Override
    public void deleteSubtasks() {
        if (getSubtasks() != null) {
            getSubtasks().forEach(prioritizedTasksSet::remove);
        }
        subtaskMap.forEach((k,v) -> historyManager.remove(v.getId()));
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.getSubtaskArray().clear();
            epic.setStatus(TaskStatus.NEW);
            calcEpicStartAndEndTime(epic);
        }
        epicMap.forEach((k,v) -> updateEpic(v));
    }

    @Override
    public void deleteEpics() {
        if (getSubtasks() != null) {
            getSubtasks().forEach(prioritizedTasksSet::remove);
        }
        subtaskMap.forEach((k,v) -> historyManager.remove(v.getId()));
        subtaskMap.clear();
        epicMap.forEach((k,v) -> historyManager.remove(v.getId()));
        epicMap.clear();
    }
}