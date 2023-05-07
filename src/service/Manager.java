package service;

import java.util.ArrayList;
import java.util.HashMap;

import model.*;

public class Manager {
    private static final String NEW = "NEW";
    private static final String INPROGRESS = "IN_PROGRESS";
    private static final String DONE = "DONE";

    private static final int TASK = 1;
    private static final int EPIC = 2;
    private static final int SUBTASK = 3;

    private HashMap<Integer, Task> taskMap;
    private HashMap<Integer, Epic> epicMap;
    private HashMap<Integer, Subtask> subtaskMap;

    private int tasksId;

    public Manager() {
        taskMap = new HashMap<>();
        epicMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        tasksId = 0;
    }

    private Integer nextId() {
        return ++tasksId;
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epicMap.values());
    }

    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        try {
            if (epicMap.containsKey(epicId)) {
                return epicMap.get(epicId).getSubtaskArray();
            } else {
                System.out.println("Не найдены подзадачи с указанным идентификатором эпик-задачи.");
            }
        }
        catch (NullPointerException npe) {
            System.out.println("Список задач пуст.");
        }
        return null;
    }

    public Task getTask(int taskId) {
        try {
            if (!taskMap.containsKey(taskId)) {
                throw new NullPointerException();
            }
            return taskMap.get(taskId);
        }
        catch (NullPointerException npe) {
            System.out.println("Не найдена задача с указанным идентификатором.");
        }
        return null;
    }

    public Subtask getSubtask(int subtaskId) {
        try {
            if (!subtaskMap.containsKey(subtaskId)) {
                throw new NullPointerException();
            }
            return subtaskMap.get(subtaskId);
        }
        catch (NullPointerException npe) {
            System.out.println("Не найдена задача с указанным идентификатором.");
        }
        return null;
    }

    public Epic getEpic(int epicId) {
        try {
            if (!epicMap.containsKey(epicId)) {
                throw new NullPointerException();
            }
            return epicMap.get(epicId);
        }
        catch (NullPointerException npe) {
            System.out.println("Не найдена задача с указанным идентификатором.");
        }
        return null;
    }


    public int addNewTask(Task task) {
        int taskId = nextId();
        task.setId(taskId);
        taskMap.putIfAbsent(taskId, task);
        return taskId;
    }

    public int addNewEpic(Epic epic) {
        int taskId = nextId();
        epic.setId(taskId);
        epicMap.putIfAbsent(taskId, epic);
        return taskId;
    }

    public int addNewSubtask(Subtask subtask) {
        int taskId = nextId();
        subtask.setId(taskId);
        subtaskMap.putIfAbsent(taskId, subtask);
        return taskId;
    }

    public void updateTask(Task task) {
        int taskId = task.getId();
        if (getTask(task.getId()) != null) {
            taskMap.replace(taskId, task);
        }
    }

    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        Epic epicFromMap = getEpic(epicId);
        if (epicFromMap != null) {
            if (!epic.getStatus().equals(epicFromMap.getStatus())) {
                System.out.println("Невозможно изменить статус эпик-задачи без учета статуса подзадач.");
            } else {
                epicMap.replace(epicId, epic);
            }
        }
    }

    public void updateSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        int epicId = subtask.getEpicId();
        Subtask subtaskFromMap = getSubtask(subtaskId);
        Epic epicOfSubtask = getEpic(epicId);
        if (subtaskFromMap != null) {
            subtaskMap.replace(subtaskId, subtask);
            boolean isNeedEpicChange = false;
            if (!epicOfSubtask.getSubtaskArray().contains(subtask)) {
                epicOfSubtask.getSubtaskArray().add(subtask);
                isNeedEpicChange = true;
            }
            if (!subtask.getStatus().equals(epicOfSubtask.getStatus())) {
                epicOfSubtask.setStatus(calcEpicStatus(epicOfSubtask));
                isNeedEpicChange = true;
            }
            if (isNeedEpicChange) {
                epicMap.replace(epicId, epicOfSubtask);
            }
        }
    }

    protected String calcEpicStatus(Epic epic) {
        String status = INPROGRESS;
        boolean isOnlyNew = true;
        boolean isOnlyDone = true;

        if (epic.getSubtaskArray() == null) {
            return NEW;
        } else {
            for (Subtask subtask : epic.getSubtaskArray()) {
                if (subtask.getStatus() != null) {
                    switch (subtask.getStatus()) {
                        case NEW:
                            isOnlyDone = false;
                            break;
                        case DONE:
                            isOnlyNew = false;
                            break;
                        default:
                            isOnlyDone = false;
                            isOnlyNew = false;
                            break;
                    }
                }
            }
        }
        if (isOnlyNew) {
            status = NEW;
        } else if (isOnlyDone) {
            status = DONE;
        }
        return status;
    }

    public void deleteTask(int taskId) {
        taskMap.remove(taskId);
    }

    public void deleteEpic(int epicId) {
        Epic epic = getEpic(epicId);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtaskArray()) {
                subtaskMap.remove(subtask.getId());
            }
            epicMap.remove(epicId);
        }
    }

    public void deleteSubtask(int subtaskId) {
        Subtask subtask = getSubtask(subtaskId);
        if (subtask != null) {
            subtaskMap.remove(subtaskId);

            int epicId = subtask.getEpicId();
            Epic epicForSubtask = getEpic(epicId);
            if (epicForSubtask != null) {
                epicForSubtask.getSubtaskArray().remove(subtask);
                String newEpicStatus = calcEpicStatus(null);
                if (!(epicForSubtask.getStatus().equals(newEpicStatus))) {
                    epicForSubtask.setStatus(newEpicStatus);
                }
                updateEpic(epicForSubtask);
            }
        }
    }

    public void deleteTasks() {
        taskMap.clear();
    }

    public void deleteSubtasks() {
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.getSubtaskArray().clear();
            epic.setStatus(NEW);
        }
        epicMap.forEach((k,v) -> {
            updateEpic(v);
        });
    }

    public void deleteEpics() {
        subtaskMap.clear();
        epicMap.clear();
    }
}