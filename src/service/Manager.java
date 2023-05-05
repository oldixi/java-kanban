package service;

import java.util.ArrayList;
import java.util.HashMap;
import model.*;

public class Manager {
    public static final String NEW = "NEW";
    public static final String INPROGRESS = "IN_PROGRESS";
    public static final String DONE = "DONE";

    public static final int TASK = 1;
    public static final int EPIC = 2;
    public static final int SUBTASK = 3;

    HashMap<Integer, Task> taskMap;
    HashMap<Integer, Epic> epicMap;
    HashMap<Integer, Subtask> subtaskMap;

    int tasksId;

    public Manager() {
        taskMap = new HashMap<>();
        epicMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        tasksId = 0;
    }
    private Integer formId() {
        return ++tasksId;
    }

    public ArrayList<Integer> getAllTasks(int taskType) {
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

    public void removeAllTasks(int taskType) {
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

    public Task getTask(int taskId, int taskType) {
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

    public int createTask(Task task, int taskType) {
        int taskId = formId();
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

    public int createEpicWithSubtasks(Epic epic, ArrayList<Subtask> subtasksArray) {
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

    public void updateTask(int taskId, Task task, int taskType) {
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

    public void removeTask(int taskId, int taskType) {
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

    public HashMap<Integer, Subtask> getAllSubtasksForEpic(int epicId) {
        HashMap<Integer, Subtask> subtaskHashMapForEpic = new HashMap<>();
        if (epicMap.containsKey(epicId)) {
            for (Subtask subtask : epicMap.get(epicId).getSubtaskArray()) {
                subtaskHashMapForEpic.put(subtask.getId(),subtask);
            }
            return subtaskHashMapForEpic;
        } else {
            System.out.println("Не найдены подзадачи с указанным идентификатором эпик-задачи.");
            return null;
        }
    }

    public String calcEpicStatus(Integer idEpic, Epic epicForCals) {
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

    public void printTask(int taskId, int taskType) {
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

    public void printEpicWithSubtasks(int epicId) {
        printTask(epicId, EPIC);
        Epic epic = (Epic) getTask(epicId, EPIC);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtaskArray()) {
                printTask(subtask.getId(), SUBTASK);
            }
        }
    }

    public static class IllegalTaskType extends Exception {
        public IllegalTaskType(String message) {
            super(message);
        }
    }
}