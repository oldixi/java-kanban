package service;

import java.util.ArrayList;
import java.util.HashMap;
import model.*;

public class Manager {
    protected static final String NEW = "NEW";
    protected static final String INPROGRESS = "IN_PROGRESS";
    protected static final String DONE = "DONE";

    protected static final int TASK = 1;
    protected static final int EPIC = 2;
    protected static final int SUBTASK = 3;

    private HashMap<Integer, Task> taskMap;
    private HashMap<Integer, Epic> epicMap;
    private HashMap<Integer, Subtask> subtaskMap;

    private int tasksId;

    protected Manager() {
        taskMap = new HashMap<>();
        epicMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        tasksId = 0;
    }

    private Integer nextId() {
        return ++tasksId;
    }

    protected ArrayList<Integer> getAllTasksId(int taskType) {
        try {
            switch (taskType) {
                case TASK:
                    return new ArrayList<>(taskMap.keySet());
                case EPIC:
                    return new ArrayList<>(epicMap.keySet());
                case SUBTASK:
                    return new ArrayList<>(subtaskMap.keySet());
                default:
                    throw new IllegalTaskType("Данный вид задач не поддерживается.");
            }
        }
        catch (IllegalTaskType itt) {
            System.out.println(itt.getMessage());
        }
        return new ArrayList<>();
    }

    protected ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicMap.values());
    }

    protected ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    protected ArrayList<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    protected void removeAllTasks(int taskType) {
        try {
            switch (taskType) {
                case TASK:
                    taskMap.clear();
                    break;
                case EPIC:
                    subtaskMap.clear();
                    epicMap.clear();
                    break;
                case SUBTASK:
                    subtaskMap.clear();
                    for (Epic epic : epicMap.values()) {
                        epic.getSubtaskArray().clear();
                        epic.setStatus(NEW);
                    }
                    epicMap.forEach((k,v) -> {
                        updateTask(k, v, EPIC);
                    });
                    break;
                default:
                    throw new IllegalTaskType("Данный вид задач не поддерживается.");
            }
        }
        catch (IllegalTaskType itt) {
            System.out.println(itt.getMessage());
        }
    }

    protected Task getTask(int taskId, int taskType) {
        try {
            switch (taskType) {
                case TASK:
                    if (!taskMap.containsKey(taskId)) {
                        throw new NullPointerException();
                    }
                    return taskMap.get(taskId);
                case EPIC:
                    return epicMap.get(taskId);
                case SUBTASK:
                    return subtaskMap.get(taskId);
                default:
                    throw new IllegalTaskType("Данный вид задач не поддерживается.");
            }
        }
        catch (NullPointerException npe) {
            System.out.println("Не найдена задача с указанным идентификатором.");
        }
        catch (IllegalTaskType itt) {
            System.out.println(itt.getMessage());
        }
        return null;
    }

    protected int createTask(Task task, int taskType) {
        int taskId = nextId();
        try {
            task.setId(taskId);
            switch (taskType) {
                case TASK:
                    taskMap.putIfAbsent(taskId, task);
                    break;
                case EPIC:
                    epicMap.putIfAbsent(taskId, (Epic) task);
                    break;
                case SUBTASK:
                    subtaskMap.putIfAbsent(taskId, (Subtask) task);
                    break;
                default:
                    throw new IllegalTaskType("Данный вид задач не поддерживается.");
            }
        }
        catch (IllegalTaskType itt) {
            System.out.println(itt.getMessage());
        }
        return taskId;
    }

    protected int createEpicWithSubtasks(Epic epic, ArrayList<Subtask> subtasksArray) {
        int epicId = createTask(epic, EPIC);
        int subtaskId;

        epic.setSubtaskArray(subtasksArray);
        epic.setStatus(calcEpicStatus(epicId, null));
        updateTask(epicId, epic, EPIC);

        for (Subtask subtask : subtasksArray) {
            subtaskId = createTask(subtask, SUBTASK);
            subtask.setId(subtaskId);
            subtask.setEpicId(epicId);
            updateTask(subtaskId, subtask, SUBTASK);
        }
        return epicId;
    }

    protected void updateTask(int taskId, Task task, int taskType) {
        if (getTask(taskId, taskType) != null) {
            try {
                switch (taskType) {
                    case TASK:
                        taskMap.replace(taskId, task);
                        break;
                    case EPIC:
                        Epic epic = (Epic) getTask(taskId, EPIC);
                        if (!epic.getStatus().equals(task.getStatus())) {
                            System.out.println("Невозможно изменить статус эпик-задачи без учета статуса подзадач.");
                        } else {
                            epicMap.replace(taskId, (Epic) task);
                        }
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) getTask(taskId, SUBTASK);
                        int epicId = subtask.getEpicId();
                        Epic epicOfSubtask = (Epic) getTask(epicId, EPIC);
                        subtaskMap.replace(taskId, (Subtask) task);
                        boolean isNeedEpicChange = false;
                        if (!epicOfSubtask.getSubtaskArray().contains(subtask)) {
                            epicOfSubtask.getSubtaskArray().add(subtask);
                            isNeedEpicChange = true;
                        }
                        if (!subtask.getStatus().equals(epicOfSubtask.getStatus())) {
                            epicOfSubtask.setStatus(calcEpicStatus(epicId, epicOfSubtask));
                            isNeedEpicChange = true;
                        }
                        if (isNeedEpicChange) {
                            epicMap.replace(epicId, epicOfSubtask);
                        }
                        break;
                    default:
                        throw new IllegalTaskType("Данный вид задач не поддерживается.");
                }
            }
            catch (IllegalTaskType itt) {
                System.out.println(itt.getMessage());
            }
        }
    }

    protected void removeTask(int taskId, int taskType) {
        if (getTask(taskId, taskType) != null) {
            try {
                switch (taskType) {
                    case TASK:
                        taskMap.remove(taskId);
                        break;
                    case EPIC:
                        Epic epic = (Epic) getTask(taskId, EPIC);
                        for (Subtask subtask : epic.getSubtaskArray()) {
                            subtaskMap.remove(subtask.getId());
                        }
                        epicMap.remove(taskId);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) getTask(taskId, SUBTASK);
                        int epicId = subtask.getEpicId();
                        Epic epicForSubtask = (Epic) getTask(epicId, EPIC);
                        subtaskMap.remove(taskId);
                        epicForSubtask.getSubtaskArray().remove(subtask);
                        String newEpicStatus = calcEpicStatus(epicId, null);
                        if (!(epicForSubtask.getStatus().equals(newEpicStatus))) {
                            epicForSubtask.setStatus(newEpicStatus);
                        }
                        updateTask(epicId, epicForSubtask, EPIC);
                        break;
                    default:
                        throw new IllegalTaskType("Данный вид задач не поддерживается.");
                }
            }
            catch (IllegalTaskType itt) {
                System.out.println(itt.getMessage());
            }
        }
    }

    protected ArrayList<Subtask> getAllSubtasksForEpic(int epicId) {
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

    protected String calcEpicStatus(Integer idEpic, Epic epicForCals) {
        String status = INPROGRESS;
        Epic epic = epicForCals;
        boolean isOnlyNew = true;
        boolean isOnlyDone = true;

        if (idEpic == null && epic == null) {
            return NEW;
        } else if (epic == null) {
            epic = epicMap.getOrDefault(idEpic, null);
        }

        if (epic.getSubtaskArray() == null) {
            isOnlyDone = false;
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

    protected void printTask(int taskId, int taskType) {
        try {
            switch (taskType) {
                case TASK:
                    Task task = getTask(taskId, taskType);
                    if (task != null) System.out.println(task.toString());
                    break;
                case EPIC:
                    Epic epic = (Epic) getTask(taskId, taskType);
                    if (epic != null) System.out.println(epic.toString());
                    break;
                case SUBTASK:
                    Subtask subtask = (Subtask) getTask(taskId, taskType);
                    if (subtask != null) System.out.println(subtask.toString());
                    break;
                default:
                    throw new IllegalTaskType("Данный вид задач не поддерживается.");
            }
        }
        catch (IllegalTaskType itt) {
            System.out.println(itt.getMessage());
        }
    }

    protected void printEpicWithSubtasks(int epicId) {
        printTask(epicId, EPIC);
        Epic epic = (Epic) getTask(epicId, EPIC);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtaskArray()) {
                printTask(subtask.getId(), SUBTASK);
            }
        }
    }

    protected static class IllegalTaskType extends Exception {
        public IllegalTaskType(String message) {
            super(message);
        }
    }
}