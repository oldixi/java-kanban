package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

//abstract class TaskManagerTest<T extends TaskManager>{
public class TaskManagerTest<T extends TaskManager> {
    private static final String TASK_NOT_FOUND = "Задача не найдена.";
    private static final String TASKS_NOT_EQUAL = "Задачи не совпадают.";
    private static final String TASKS_NOT_RETURN = "Задачи не возвращаются.";
    private static final String TASKS_COUNT_FAIL = "Неверное количество задач.";
    private static final String TASK_UPDATE_FAIL = "Задача не изменилась.";
    private static final String TASK_UPDATE = "Задача не изменилась.";
    private static final String TASK_UPDATE_FOR_NULL = "Сохранилась пустая задача";
    private static final String TASK_DELETE_FAIL = "Задача не удалена.";
    private static final String TASKS_COUNT_NOT_CHANGE = "Количество сохраненных задач не изменилось.";
    private static final String TASK_DELETE_WRONG = "Задача удалена ошибочно.";
    private static final String TASK_LIST_WRONG = "Списки задач не совпадают.";
    private static final String TASKS_NOT_ALL_DELETED = "Удалены не все задачи.";
    private static final String SUBTASKS_NOT_ALL_DELETED = "Удалены не все подзадачи.";
    private static final String WRONG_LINE_COUNT = "Не совпадает количество строк в файле с сохраненными данными.";
    private static final String TASK_SERIALIZATION_FAIL = "Задача выгружена некорректно.";
    private static final String TASKS_NOT_INFILE = "Нет задач в файле.";
    private static final String TASKS_DESERIALIZATION_FAIL ="Задачи не загрузились.";
    private static final String TASKS_DESERIALIZED_COUNT_WRONG = "Загружено неверное количество задач.";
    private static final String HISTORY_DESERIALIZED_COUNT_WRONG = "Загружено неверное количество задач в историю просмотров.";
    private static final String TASK_DUR_WRONG = "Длительность выполнения эпика рассчитана неверно.";
    private static final String TASK_STARTTIME_WRONG = "Дата начала выполнения эпика рассчитана неверно.";
    private static final String TASK_ENDTIME_WRONG = "Дата планового окончания выполнения эпика рассчитана неверно.";
    private static final String TASK_DUR_FAIL = "Не рассчитана длительность выполнения эпик.";
    private static final String TASK_STARTTIME_FAIL = "Не рассчитана дата начала выполнения эпика.";
    private static final String TASK_ENDTIME_FAIL = "Не рассчитана дата планового окончания выполнения эпика.";
    private static final String TASK_NOT_CROSSED = "Задача не пересекается с другими задачами: дата начала работ по задаче не должна меняться.";
    private static final String TASK_CROSSED = "Задача пересекается с другими задачами: дата начала работ должна быть скринута.";

    InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
    FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(FileBackedTasksManager.FILEPATH);
    TaskManager taskManager = Managers.getDefault();

    Task task1 = new Task("Задача1", "Выполнить задачу 1", TaskStatus.NEW,10, LocalDateTime.now().minusDays(60));
    Task task2 = new Task("Задача2", "Выполнить задачу 2");
    Task task3 = new Task("Задача3", "Выполнить задачу 3");
    Task task4 = new Task("Задача4", "Выполнить задачу 4");
    Task task5 = new Task("Задача5", "Выполнить задачу 5");

    Subtask subtask1 = new Subtask("Подзадача1", "Выполнить подзадачу 1");
    Subtask subtask2 = new Subtask("Подзадача2", "Выполнить подзадачу 2");
    ArrayList<Subtask> subtasks = new ArrayList<>(List.of(subtask1,subtask2));
    Epic epic1 = new Epic("Эпик1", "Выполнить Эпик1", subtasks);

    Epic epic2 = new Epic("Эпик2", "Выполнить Эпик2");
    Subtask subtask3 = new Subtask("Подзадача3", "Выполнить подзадачу 3");
    Subtask subtask4 = new Subtask("Подзадача4", "Выполнить подзадачу 4");
    ArrayList<Subtask> subtasks2 = new ArrayList<>(List.of(subtask3,subtask4));

    Subtask subtask5 = new Subtask("Подзадача5", "Выполнить подзадачу 5");
    Subtask subtask6 = new Subtask("Подзадача6", "Выполнить подзадачу 6");
    ArrayList<Subtask> subtasks3 = new ArrayList<>(List.of(subtask5,subtask6));
    Epic epic3 = new Epic("Эпик3", "Выполнить Эпик3", subtasks3);

    Subtask subtask7 = new Subtask("Подзадача7", "Выполнить подзадачу 7");
    Subtask subtask8 = new Subtask("Подзадача8", "Выполнить подзадачу 8", TaskStatus.DONE);
    ArrayList<Subtask> subtasks4 = new ArrayList<>(List.of(subtask7,subtask8));
    Epic epic4 = new Epic("Эпик4", "Выполнить Эпик4", subtasks4);

    Subtask subtask9 = new Subtask("Подзадача9", "Выполнить подзадачу 9");
    ArrayList<Subtask> subtasks5 = new ArrayList<>(List.of(subtask9));
    Epic epic5 = new Epic("Эпик5", "Выполнить Эпик5", subtasks5);

    Epic epic6 = new Epic("Эпик6", "Выполнить Эпик6");

    Subtask subtaskNEW1 = new Subtask("ПодзадачаNEW1", "Выполнить подзадачу NEW1");
    Subtask subtaskNEW2 = new Subtask("ПодзадачаNEW2", "Выполнить подзадачу NEW2");
    Subtask subtaskIN_PROGRESS1 = new Subtask("ПодзадачаIN_PROGRESS1", "Выполнить подзадачу IN_PROGRESS1", TaskStatus.IN_PROGRESS);
    Subtask subtaskIN_PROGRESS2 = new Subtask("ПодзадачаIN_PROGRESS2", "Выполнить подзадачу IN_PROGRESS2", TaskStatus.IN_PROGRESS);
    Subtask subtaskDONE1 = new Subtask("ПодзадачаDONE1", "Выполнить подзадачу DONE1", TaskStatus.DONE);
    Subtask subtaskDONE2 = new Subtask("ПодзадачаDONE2", "Выполнить подзадачу DONE2", TaskStatus.DONE);
    ArrayList<Subtask> subtasksNEW = new ArrayList<>(List.of(subtaskNEW1, subtaskNEW2));
    ArrayList<Subtask> subtasksDONE = new ArrayList<>(List.of(subtaskDONE1, subtaskDONE2));
    ArrayList<Subtask> subtasksIN_PROGRESS = new ArrayList<>(List.of(subtaskIN_PROGRESS1, subtaskIN_PROGRESS2));
    ArrayList<Subtask> subtasksNEWandDONE = new ArrayList<>(List.of(subtaskNEW1, subtaskDONE2));
    Epic epicNEW1 = new Epic("ЭпикNEW1", "Выполнить ЭпикNEW1");
    Epic epicNEW2 = new Epic("ЭпикNEW2", "Выполнить ЭпикNEW2", subtasksNEW);
    Epic epicDONE = new Epic("ЭпикDONE", "Выполнить ЭпикDONE", subtasksDONE);
    Epic epicIN_PROGRESS1 = new Epic("ЭпикIN_PROGRESS1", "Выполнить ЭпикIN_PROGRESS1", subtasksIN_PROGRESS);
    Epic epicIN_PROGRESS2 = new Epic("ЭпикIN_PROGRESS1", "Выполнить ЭпикIN_PROGRESS1", subtasksNEWandDONE);

    Subtask subtaskNow = new Subtask("ПодзадачаNow", "Выполнить подзадачуNow");
    Subtask subtaskBeforeNow = new Subtask("ПодзадачаBeforeNow", "Выполнить подзадачуBeforeNow");
    Subtask subtaskAfterNow = new Subtask("ПодзадачаAfterNow ", "Выполнить подзадачуAfterNow ");
    Subtask subtaskDur0 = new Subtask("ПодзадачаDur0", "Выполнить подзадачуDur0");
    Subtask subtaskStrtNull = new Subtask("ПодзадачаStrtNull", "Выполнить подзадачуStrtNull");
    ArrayList<Subtask> subtasksStrt = new ArrayList<>(List.of(subtaskBeforeNow, subtaskNow, subtaskAfterNow));
    ArrayList<Subtask> subtasksNull = new ArrayList<>(List.of(subtaskStrtNull, subtaskDur0));
    Epic epic = new Epic("Эпик", "Выполнить Эпик");
    Epic epicStrt = new Epic("ЭпикStrt", "Выполнить ЭпикStrt", subtasksStrt);
    Epic epicNull = new Epic("ЭпикNull", "Выполнить ЭпикNull", subtasksNull);

    @Test
    public void shouldAddNewTask() {
        final int taskId = taskManager.addNewTask(task1);
        final Task savedTask = taskManager.getTask(taskId);
        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(savedTask, TASK_NOT_FOUND);
        assertEquals(task1, savedTask, TASKS_NOT_EQUAL);
        assertNotNull(tasks, TASKS_NOT_RETURN);
        assertEquals(1, tasks.size(), TASKS_COUNT_FAIL);
        assertEquals(task1, tasks.get(0), TASKS_NOT_EQUAL);
    }

    @Test
    public void shouldAddNewEpic() {
        final int taskId = taskManager.addNewEpic(epic1);
        final Epic savedTask = taskManager.getEpic(taskId);
        final List<Epic> tasks = taskManager.getEpics();

        assertNotNull(savedTask, TASK_NOT_FOUND);
        assertEquals(epic1, savedTask, TASKS_NOT_EQUAL);
        assertNotNull(tasks, TASKS_NOT_RETURN);
        assertEquals(1, tasks.size(), TASKS_COUNT_FAIL);
        assertEquals(epic1, tasks.get(0), TASKS_NOT_EQUAL);
    }

    @Test
    public void shouldAddNewSubtask() {
        final int epicId = taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epicId);
        final int taskId = taskManager.addNewSubtask(subtask1);
        final Subtask savedTask = taskManager.getSubtask(taskId);
        final List<Subtask> tasks = taskManager.getSubtasks();

        assertNotNull(savedTask, TASK_NOT_FOUND);
        assertEquals(subtask1, savedTask, TASKS_NOT_EQUAL);
        assertNotNull(tasks, TASKS_NOT_RETURN);
        assertEquals(1, tasks.size(), TASKS_COUNT_FAIL);
        assertEquals(subtask1, tasks.get(0), TASKS_NOT_EQUAL);
    }

    @Test
    public void shouldUpdateTask() {
        final int taskId = taskManager.addNewTask(task2);
        final Task savedTask = taskManager.getTask(taskId);
        final Task taskClone = task2.clone();
        taskClone.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.updateTask(taskClone);

        assertNotEquals(savedTask, taskManager.getTask(taskId), TASK_UPDATE_FAIL);
        assertEquals(taskClone, taskManager.getTask(taskId), TASK_UPDATE_FAIL);
    }

    @Test
    public void shouldNotUpdateTaskBadId() {
        final int taskId = taskManager.addNewTask(task3);
        final Task savedTask = taskManager.getTask(taskId);
        final Task taskClone = task3.clone();
        taskClone.setId(-1);

        taskManager.updateTask(taskClone);

        assertEquals(savedTask, taskManager.getTask(taskId), TASK_UPDATE);
        assertNotEquals(taskClone, taskManager.getTask(taskId), TASK_UPDATE);
    }

    @Test
    public void shouldNotUpdateTaskNullId() {
        final Task savedTask = taskManager.getTask(0);

        taskManager.updateTask(task4);

        assertNull(savedTask, TASK_UPDATE_FOR_NULL);
        assertNotEquals(task4, taskManager.getTask(0), TASK_UPDATE);
    }

    @Test
    public void shouldUpdateSubtaskStatus() {
        final int epicId = taskManager.addNewEpic(epic1);
        subtask2.setEpicId(epicId);
        final int taskId = taskManager.addNewSubtask(subtask2);
        final Subtask savedTask = taskManager.getSubtask(taskId);
        final Subtask subtaskClone = subtask2.clone();
        subtaskClone.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.updateSubtask(subtaskClone);

        assertNotEquals(savedTask, taskManager.getSubtask(taskId), TASK_UPDATE_FAIL);
        assertEquals(subtaskClone, taskManager.getSubtask(taskId), TASK_UPDATE_FAIL);
    }

    @Test
    public void shouldUpdateSubtaskEpicId() {
        final int epicId = taskManager.addNewEpic(epic2);
        subtask3.setEpicId(epicId);
        final int taskId = taskManager.addNewSubtask(subtask3);
        final Subtask savedTask = taskManager.getSubtask(taskId);
        final int epicNewId = taskManager.addNewEpic(epic3);
        Subtask subtaskClone = subtask3.clone();
        subtaskClone.setEpicId(epicNewId);

        taskManager.updateSubtask(subtaskClone);

        assertNotEquals(savedTask, taskManager.getSubtask(taskId), TASK_UPDATE_FAIL);
        assertEquals(subtaskClone, taskManager.getSubtask(taskId), TASK_UPDATE_FAIL);
    }

    @Test
    public void shouldNotUpdateSubtaskNullId() {
        final int epicId = taskManager.addNewEpic(epic2);
        subtask3.setEpicId(epicId);
        final Subtask savedTask = taskManager.getSubtask(0);
        final Subtask subtaskClone = subtask3.clone();
        subtaskClone.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtaskClone);

        assertNull(savedTask, TASK_UPDATE_FOR_NULL);
        assertNotEquals(subtaskClone, taskManager.getSubtask(0), TASK_UPDATE);
    }

    @Test
    public void shouldUpdateEpic() {
        final int epicId = taskManager.addNewEpic(epic3);
        final Epic savedTask = taskManager.getEpic(epicId);
        subtask4.setEpicId(epicId);
        subtask5.setEpicId(epicId);
        subtask6.setEpicId(epicId);
        final int subtask4Id = taskManager.addNewSubtask(subtask4);
        final int subtask5Id = taskManager.addNewSubtask(subtask5);
        final int subtask6Id = taskManager.addNewSubtask(subtask6);
        final ArrayList<Subtask> subtaskList = new ArrayList<>(List.of(subtask4, subtask5, subtask6));
        final Epic epicClone = epic3.clone();
        epicClone.setSubtaskArray(subtaskList);

        taskManager.updateEpic(epicClone);
        final Epic afterUpdateTask = taskManager.getEpic(epicId);

        assertEquals(List.of(subtask4, subtask5, subtask6), afterUpdateTask.getSubtaskArray(), TASK_LIST_WRONG);
        assertNotEquals(savedTask, afterUpdateTask, TASK_UPDATE_FAIL);
        assertEquals(epicClone, afterUpdateTask, TASK_UPDATE_FAIL);
    }

    @Test
    public void shouldNotUpdateEpicStatus() {
        final int epicId = taskManager.addNewEpic(epic3);
        final Epic savedTask = taskManager.getEpic(epicId);
        final Epic epicClone = epic3.clone();
        epicClone.setStatus(TaskStatus.DONE);
        taskManager.updateEpic(epicClone);

        assertEquals(savedTask, taskManager.getEpic(epicId), TASK_UPDATE);
        assertNotEquals(epicClone, taskManager.getEpic(epicId), TASK_UPDATE);
    }

    @Test
    public void shouldDeleteTask() {
        final int taskId = taskManager.addNewTask(task5);
        final int taskMapSize = taskManager.getTasks().size();
        taskManager.deleteTask(taskId);

        assertNull(taskManager.getTask(taskId), TASK_DELETE_FAIL);
        assertEquals(taskMapSize - 1, taskManager.getTasks().size(), TASKS_COUNT_NOT_CHANGE);
    }

    @Test
    public void shouldNotDeleteTaskBadId() {
        final int taskId = taskManager.addNewTask(task5);
        final Task savedTask = taskManager.getTask(taskId);
        final int taskMapSize = taskManager.getTasks().size();

        taskManager.deleteTask(-1);

        assertTrue(taskManager.getTasks().contains(savedTask), TASK_DELETE_WRONG);
        assertEquals(taskMapSize, taskManager.getTasks().size(), TASKS_COUNT_NOT_CHANGE);
    }

    @Test
    public void shouldDeleteSubtask() {
        final int epicId = taskManager.addNewEpic(epic4);
        subtask7.setEpicId(epicId);
        subtask8.setEpicId(epicId);
        final int subtask7Id = taskManager.addNewSubtask(subtask7);
        final int subtask8Id = taskManager.addNewSubtask(subtask8);
        final int taskMapSize = taskManager.getSubtasks().size();

        final Subtask subtask = taskManager.getSubtask(subtask7Id);
        final int epicIdFromSubtask = subtask.getEpicId();
        final Epic epic = taskManager.getEpic(epicIdFromSubtask);
        final ArrayList<Subtask> subtaskArray = epic.getSubtaskArray();
        final int subtaskArraySize = subtaskArray.size();

        taskManager.deleteSubtask(subtask.getId());

        assertNull(taskManager.getSubtask(subtask7Id), TASK_DELETE_FAIL);
        assertEquals(taskMapSize - 1, taskManager.getSubtasks().size(), TASKS_COUNT_NOT_CHANGE);
        assertEquals(subtaskArraySize - 1, epic.getSubtaskArray().size(), TASKS_COUNT_NOT_CHANGE);
    }

    @Test
    public void shouldNotDeleteSubtaskBadId() {
        final int epicId = taskManager.addNewEpic(epic5);
        subtask9.setEpicId(epicId);
        final int taskId = taskManager.addNewSubtask(subtask9);
        final Subtask savedTask = taskManager.getSubtask(taskId);
        final int taskMapSize = taskManager.getSubtasks().size();

        taskManager.deleteSubtask(-1);

        assertTrue(taskManager.getSubtasks().contains(savedTask), TASK_DELETE_WRONG);
        assertEquals(taskMapSize, taskManager.getSubtasks().size(), TASKS_COUNT_NOT_CHANGE);
    }

    @Test
    public void shouldDeleteEpic() {
        final int epicId = taskManager.addNewEpic(epic4);
        final Epic epic = taskManager.getEpic(epicId);
        final int taskMapSize = taskManager.getEpics().size();

        taskManager.deleteEpic(epicId);

        assertNull(taskManager.getEpic(epicId), TASK_DELETE_FAIL);
        assertEquals(taskMapSize - 1, taskManager.getEpics().size(), TASKS_COUNT_NOT_CHANGE);
        epic.getSubtaskArray().forEach((v) -> assertNull(taskManager.getSubtask(v.getId())
                , "Не удаляются подзадачи при удалении эпика."));
    }

    @Test
    public void shouldNotDeleteEpicBadId() {
        final int taskId = taskManager.addNewEpic(epic6);
        final Epic savedTask = taskManager.getEpic(taskId);
        final int taskMapSize = taskManager.getEpics().size();

        taskManager.deleteEpic(-1);

        assertTrue(taskManager.getEpics().contains(savedTask), TASK_DELETE_WRONG);
        assertEquals(taskMapSize, taskManager.getEpics().size(), TASKS_COUNT_NOT_CHANGE);
    }

    @Test
    public void shouldDeleteAllTasks() {
        taskManager.deleteTasks();

        assertEquals(0, taskManager.getTasks().size(), TASKS_NOT_ALL_DELETED);
    }

    @Test
    public void shouldDeleteAllSubtasks() {
        taskManager.deleteSubtasks();

        assertEquals(0, taskManager.getSubtasks().size(), SUBTASKS_NOT_ALL_DELETED);
        taskManager.getEpics().forEach((v) -> assertEquals(0, v.getSubtaskArray().size()
                ,"Не у всех эпиков удалены подзадачи"));
    }

    @Test
    public void shouldDeleteAllEpics() {
        taskManager.deleteEpics();

        assertEquals(0, taskManager.getEpics().size(), TASKS_NOT_ALL_DELETED);
        assertEquals(0, taskManager.getSubtasks().size(), SUBTASKS_NOT_ALL_DELETED);
    }

    @Test
    public void shouldReturnMinStrtAndSumDur() {
        LocalDateTime now = LocalDateTime.now();
        final int epicStrtId = taskManager.addNewEpic(epicStrt);
        subtaskBeforeNow.setStartTime(now.minusDays(365));
        subtaskBeforeNow.setDuration(10);
        subtaskBeforeNow.setEpicId(epicStrtId);
        subtaskNow.setStartTime(now);
        subtaskNow.setDuration(10);
        subtaskNow.setEpicId(epicStrtId);
        subtaskAfterNow.setStartTime(now.plusDays(20));
        subtaskAfterNow.setEpicId(epicStrtId);
        taskManager.addNewSubtask(subtaskBeforeNow);
        taskManager.addNewSubtask(subtaskNow);
        taskManager.addNewSubtask(subtaskAfterNow);

        assertEquals(20, taskManager.getEpic(epicStrtId).getDuration(), TASK_DUR_WRONG);
        assertEquals(now.minusDays(365), taskManager.getEpic(epicStrtId).getStartTime()
                , TASK_STARTTIME_WRONG);
        assertEquals(now.minusDays(365).plusMinutes(20), taskManager.getEpic(epicStrtId).getEndTime()
                , TASK_ENDTIME_WRONG);
        assertNotNull(taskManager.getEpic(epicStrtId).getDuration(), TASK_DUR_FAIL);
        assertNotNull(taskManager.getEpic(epicStrtId).getStartTime(), TASK_STARTTIME_FAIL);
        assertNotNull(taskManager.getEpic(epicStrtId).getEndTime(), TASK_ENDTIME_FAIL);
    }

    @Test
    public void shouldReturnNullStrtOrDur() {
        LocalDateTime now = LocalDateTime.now();
        final int epictId = taskManager.addNewEpic(epicNull);
        subtaskDur0.setStartTime(now);
        subtaskDur0.setEpicId(epictId);
        subtaskStrtNull.setDuration(6000);
        subtaskDur0.setEpicId(epictId);
        taskManager.addNewSubtask(subtaskDur0);
        taskManager.addNewSubtask(subtaskNow);
        taskManager.addNewSubtask(subtaskAfterNow);

        assertEquals(6000, taskManager.getEpic(epictId).getDuration(), TASK_DUR_WRONG);
        assertEquals(now, taskManager.getEpic(epictId).getStartTime(), TASK_STARTTIME_WRONG);
        assertEquals(now.plusMinutes(6000), taskManager.getEpic(epictId).getEndTime(), TASK_ENDTIME_WRONG);
        assertNotNull(taskManager.getEpic(epictId).getDuration(), TASK_DUR_FAIL);
        assertNotNull(taskManager.getEpic(epictId).getStartTime(), TASK_STARTTIME_FAIL);
        assertNotNull(taskManager.getEpic(epictId).getEndTime(), TASK_ENDTIME_FAIL);
    }

    @Test
    public void shouldReturnStrtWhenNotCrossed() {
        LocalDateTime now = LocalDateTime.now();
        final int epicStrtId = taskManager.addNewEpic(epicStrt);
        subtaskBeforeNow.setStartTime(now.minusDays(365));
        subtaskBeforeNow.setDuration(10);
        subtaskBeforeNow.setEpicId(epicStrtId);
        subtaskNow.setStartTime(now);
        subtaskNow.setDuration(10);
        subtaskNow.setEpicId(epicStrtId);
        taskManager.addNewSubtask(subtaskBeforeNow);
        taskManager.addNewSubtask(subtaskNow);
        task2.setStartTime(now.plusMinutes(60));
        task2.setDuration(60);
        taskManager.addNewTask(task2);

        assertNotNull(task2.getStartTime(), TASK_NOT_CROSSED);
        assertEquals(now.plusMinutes(60), task2.getStartTime(), TASK_NOT_CROSSED);
        assertNotNull(subtaskBeforeNow.getStartTime(), TASK_NOT_CROSSED);
        assertEquals(now.minusDays(365), subtaskBeforeNow.getStartTime(), TASK_NOT_CROSSED);
        assertNotNull(subtaskNow.getStartTime(), TASK_NOT_CROSSED);
        assertEquals(now, subtaskNow.getStartTime(), TASK_NOT_CROSSED);
    }

    @Test
    public void shouldReturnNullStrtWhenCrossed() {
        LocalDateTime now = LocalDateTime.now();
        task2.setStartTime(now.plusMinutes(60));
        task2.setDuration(60);
        taskManager.addNewTask(task2);
        task3.setStartTime(now.plusMinutes(80));
        taskManager.addNewTask(task3);

        assertNull(task3.getStartTime(), TASK_CROSSED);
    }

    @Test
    public void shouldReturnNullStrtWhenChangedAndCrossed() {
        LocalDateTime now = LocalDateTime.now();
        task2.setStartTime(now.plusMinutes(60));
        task2.setDuration(60);
        taskManager.addNewTask(task2);
        task3.setStartTime(now);
        taskManager.addNewTask(task3);
        task3.setStartTime(now.plusMinutes(80));
        taskManager.updateTask(task3);

        assertNull(task3.getStartTime(), TASK_CROSSED);
    }

    @Test
    public void shouldReturnStrtWhenChangedAndNotCrossed() {
        LocalDateTime now = LocalDateTime.now();
        final int epicStrtId = taskManager.addNewEpic(epicStrt);
        subtaskBeforeNow.setStartTime(now.minusMinutes(10));
        subtaskBeforeNow.setDuration(10);
        subtaskBeforeNow.setEpicId(epicStrtId);
        subtaskNow.setStartTime(now);
        subtaskNow.setDuration(10);
        subtaskNow.setEpicId(epicStrtId);
        taskManager.addNewSubtask(subtaskBeforeNow);
        taskManager.addNewSubtask(subtaskNow);
        subtaskBeforeNow.setStartTime(now.minusMinutes(30));
        taskManager.updateTask(subtaskBeforeNow);

        assertNotNull(subtaskBeforeNow.getStartTime(), TASK_NOT_CROSSED);
        assertEquals(now.minusMinutes(30), subtaskBeforeNow.getStartTime(), TASK_NOT_CROSSED);
    }

    @Test
    public void shouldReturnNullStrtWhenEqualStrtOrEnd() {
        LocalDateTime now = LocalDateTime.now();
        task2.setStartTime(now);
        task2.setDuration(60);
        taskManager.addNewTask(task2);
        task3.setStartTime(now.plusMinutes(20));
        taskManager.addNewTask(task3);
        task4.setStartTime(now.minusMinutes(20));
        task4.setDuration(40);
        taskManager.addNewTask(task4);

        assertNull(task3.getStartTime(), TASK_CROSSED);
        assertNull(task4.getStartTime(), TASK_CROSSED);
    }

    @Test
    public void shouldReturnNEWWhenSubtasksArrayBlank() {
        assertEquals(TaskStatus.NEW, inMemoryTaskManager.calcEpicStatus(epicNEW1)
                ,"Статус эпик не NEW для пустого списка подзадач.");
    }

    @Test
    public void shouldReturnNEWWhenAllSubtasksNEW() {
        assertEquals(TaskStatus.NEW, inMemoryTaskManager.calcEpicStatus(epicNEW2)
                ,"Статус эпик не NEW для списка подзадач в статусе NEW.");
    }

    @Test
    public void shouldReturnDONEWhenAllSubtasksDONE() {
        assertEquals(TaskStatus.DONE, inMemoryTaskManager.calcEpicStatus(epicDONE)
                ,"Статус эпик не DONE для списка подзадач в статусе DONE.");
    }

    @Test
    public void shouldReturnIN_PROGRESSWhenAllSubtasksIN_PROGRESS() {
        assertEquals(TaskStatus.IN_PROGRESS, inMemoryTaskManager.calcEpicStatus(epicIN_PROGRESS1)
                ,"Статус эпик не IN_PROGRESS для списка подзадач в статусе IN_PROGRESS.");
    }

    @Test
    public void shouldReturnIN_PROGRESSWhenSubtasksNEWandDONE() {
        assertEquals(TaskStatus.IN_PROGRESS, inMemoryTaskManager.calcEpicStatus(epicIN_PROGRESS2)
                ,"Статус эпик не IN_PROGRESS для списка подзадач в статусе DONE и NEW.");
    }

    private long getTasksInFileCnt() {
        long fileSize = 0;
        if (Files.exists(FileBackedTasksManager.FILEPATH)) {
            try {
                fileSize = Files.lines(FileBackedTasksManager.FILEPATH)
                        .filter(line -> !line.equals(FileBackedTasksManager.TITLE)
                                        && !line.isBlank()
                                        && (line.contains("TASK")
                                        || line.contains("SUBTASK")
                                        || line.contains("EPIC")
                                        )
                        )
                        .count();
            } catch (IOException ioe) {
                fileSize = -1;
            }
        }
        return fileSize;
    }

    private void printTasksFromFile() {
        if (Files.exists(FileBackedTasksManager.FILEPATH)) {
            try {
                System.out.println("***");
                Files.lines(FileBackedTasksManager.FILEPATH)
                        .filter(line -> !line.equals(FileBackedTasksManager.TITLE)
                                        && !line.isBlank()
                                        && (line.contains("TASK")
                                        || line.contains("SUBTASK")
                                        || line.contains("EPIC")
                                )
                        )
                        .forEach(System.out::println);
                System.out.println("***ALL***");
                Files.lines(FileBackedTasksManager.FILEPATH)
                        .forEach(System.out::println);
                System.out.println("***");
            } catch (IOException ioe) {
            }
        }
    }

    private int tasksInMemoryCount() {
        return taskManager.getTasks().size()
                + taskManager.getSubtasks().size()
                + taskManager.getEpics().size();
    }

    @Test
    public void shouldSerializeAllTasks() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewTask(task3);
        taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        subtask2.setEpicId(epic1.getId());
        taskManager.addNewEpic(epic2);
        taskManager.addNewEpic(epic3);
        subtask5.setEpicId(epic1.getId());
        subtask6.setEpicId(epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        taskManager.addNewSubtask(subtask5);
        taskManager.addNewSubtask(subtask6);
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getTask(task3.getId());
        taskManager.getEpic(epic3.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getTask(task2.getId());
        assertEquals(tasksInMemoryCount() , getTasksInFileCnt(), WRONG_LINE_COUNT);
    }

    private String getFileLineWithTasks() {
        String lineTask = "";
        if (Files.exists(FileBackedTasksManager.FILEPATH)) {
            try {
                lineTask = Files.lines(FileBackedTasksManager.FILEPATH)
                        .filter(line -> !line.equals(FileBackedTasksManager.TITLE)
                                        && !line.isBlank()
                                        && (line.contains("TASK")
                                            || !line.contains("SUBTASK")
                                            || !line.contains("EPIC")
                                        )
                                )
                        .collect(Collectors.joining("\n"));
            } catch (IOException ioe) {
                lineTask = "dummy";
            }
        }
        return lineTask;
    }

    @Test
    public void shouldSerializeTask() {
        taskManager.addNewTask(task1);
        String strTask = String.join(","
                ,String.valueOf(task1.getId())
                ,"TASK"
                ,task1.getName()
                ,task1.getStatus().toString()
                ,task1.getDsc()
                ," "
                ,task1.getStartTime().format(Task.FORMATTER)
                ,String.valueOf(task1.getDuration()));

        assertEquals(strTask, getFileLineWithTasks(), TASK_SERIALIZATION_FAIL);
        assertNotNull(getFileLineWithTasks(), TASKS_NOT_INFILE);
    }

    @Test
    public void shouldSerializeTaskNullStartTimeAndDuration() {
        taskManager.addNewTask(task2);
        String strTask = String.join(","
                ,String.valueOf(task2.getId())
                ,"TASK"
                ,task2.getName()
                ,task2.getStatus().toString()
                ,task2.getDsc()
                ," "
                ," "
                ," "
                ,"");

        assertEquals(strTask, getFileLineWithTasks(), TASK_SERIALIZATION_FAIL);
        assertNotNull(getFileLineWithTasks(), TASKS_NOT_INFILE);
    }

    @Test
    public void shouldSerializeEpicAndSubtask() {
        taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        subtask1.setDuration(10);
        subtask1.setStartTime(LocalDateTime.now());
        subtask2.setEpicId(epic1.getId());
        subtask2.setDuration(5);
        subtask2.setStartTime(LocalDateTime.now().minusDays(300));
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        String strEpic = String.join(","
                ,String.valueOf(epic1.getId())
                ,"EPIC"
                ,epic1.getName()
                ,epic1.getStatus().toString()
                ,epic1.getDsc()
                ," "
                ,epic1.getStartTime().format(Task.FORMATTER)
                ,String.valueOf(epic1.getDuration()));
        String strSubtask1 = String.join(","
                ,String.valueOf(subtask1.getId())
                ,"SUBTASK"
                ,subtask1.getName()
                ,subtask1.getStatus().toString()
                ,subtask1.getDsc()
                ,String.valueOf(subtask1.getEpicId())
                ,subtask1.getStartTime().format(Task.FORMATTER)
                ,String.valueOf(subtask1.getDuration()));
        String strSubtask2 = String.join(","
                ,String.valueOf(subtask2.getId())
                ,"SUBTASK"
                ,subtask2.getName()
                ,subtask2.getStatus().toString()
                ,subtask2.getDsc()
                ,String.valueOf(subtask2.getEpicId())
                ,subtask2.getStartTime().format(Task.FORMATTER)
                ,String.valueOf(subtask2.getDuration()));

        assertEquals(strEpic + '\n' + strSubtask1 + '\n' + strSubtask2
                , getFileLineWithTasks(), TASK_SERIALIZATION_FAIL);
        assertNotNull(getFileLineWithTasks(), TASKS_NOT_INFILE);
    }

    @Test
    public void shouldNotSerializeSubtaskWithoutEpic() {
        taskManager.addNewSubtask(subtask2);

        assertEquals("",getFileLineWithTasks(), "Задача выгружена ошибочно.");
    }

    @Test
    public void shouldSerializeEpicsWithoutSubtasks() {
        taskManager.addNewEpic(epic1);
        String strEpic = String.join(","
                ,String.valueOf(epic1.getId())
                ,"EPIC"
                ,epic1.getName()
                ,epic1.getStatus().toString()
                ,epic1.getDsc()
                ," "
                ," "
                ," "
                ,"");

        assertEquals(strEpic, getFileLineWithTasks(), TASK_SERIALIZATION_FAIL);
        assertNotNull(getFileLineWithTasks(), TASKS_NOT_INFILE);;
    }

    private String getFileLineWithHistory() {
        String historyLine = "";
        if (Files.exists(FileBackedTasksManager.FILEPATH)) {
            try {
                historyLine = Files.lines(FileBackedTasksManager.FILEPATH)
                        .filter(line -> !line.equals(FileBackedTasksManager.TITLE)
                                && !line.isBlank()
                                && !line.contains("TASK")
                                && !line.contains("SUBTASK")
                                && !line.contains("EPIC")
                        )
                        .collect(Collectors.joining());
            } catch (IOException ioe) {
                historyLine = "dummy";
            }
        }
        return historyLine;
    }

    @Test
    public void shouldSerializeHistory() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewTask(task3);
        taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addNewEpic(epic2);
        subtask5.setEpicId(epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask6);
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getTask(task3.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getTask(task2.getId());

        assertEquals("1,6,5,3,4,2", getFileLineWithHistory()
                , "История выгружена некорректно.");
    }

    @Test
    public void shouldSerializeBlankHistory() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewTask(task3);
        taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        taskManager.addNewEpic(epic2);
        subtask5.setEpicId(epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask6);

        assertEquals("", getFileLineWithHistory(), "История должна быть пустой.");
    }

    @Test
    public void shouldNotSerializeTasksBlankMaps() {
        taskManager.addNewTask(task1);
        taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        subtask2.setEpicId(epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        assertEquals(tasksInMemoryCount() , getTasksInFileCnt(), WRONG_LINE_COUNT);
    }

    @Test
    public void shouldDeserializeTasks() {
        taskManager.addNewTask(task1);
        taskManager.addNewEpic(epic1);
        taskManager.addNewTask(task3);
        subtask1.setEpicId(epic1.getId());
        subtask2.setEpicId(epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewTask(task4);
        taskManager.addNewSubtask(subtask2);
        taskManager.addNewTask(task5);

        taskManager.getTask(task1.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getTask(task1.getId());

        TaskManager manager = FileBackedTasksManager.loadFromFile(FileBackedTasksManager.FILEPATH);
        taskManager.addNewTask(task2);
        taskManager.getTask(task2.getId());

        assertNotNull(taskManager.getTasks(), TASKS_DESERIALIZATION_FAIL);
        assertNotNull(taskManager.getSubtasks(), TASKS_DESERIALIZATION_FAIL);
        assertNotNull(taskManager.getEpics(), TASKS_DESERIALIZATION_FAIL);
        assertEquals(getTasksInFileCnt(), tasksInMemoryCount(), TASKS_DESERIALIZED_COUNT_WRONG);

        final int countTasksInHistory = getFileLineWithHistory().split(",").length;

        assertNotNull(taskManager.getHistoryManager().getHistory(), "История не загрузилась.");
        assertEquals(countTasksInHistory, taskManager.getHistoryManager().getHistory().size()
                , HISTORY_DESERIALIZED_COUNT_WRONG);
    }

    @Test
    public void shouldDeserializeTasksBlankHistory() {
        taskManager.addNewTask(task1);
        taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epic1.getId());
        subtask2.setEpicId(epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        TaskManager manager = FileBackedTasksManager.loadFromFile(FileBackedTasksManager.FILEPATH);
        final boolean isBlankHistory = getFileLineWithHistory().isBlank();

        assertTrue(isBlankHistory, "Загрузилась не существующая история.");
    }

    @Test
    public void shouldNotDeserializeTasksBlank() {
        TaskManager manager = FileBackedTasksManager.loadFromFile(FileBackedTasksManager.FILEPATH);
        assertTrue(taskManager.getTasks().isEmpty(), TASKS_DESERIALIZED_COUNT_WRONG);
        assertTrue(taskManager.getSubtasks().isEmpty(), TASKS_DESERIALIZED_COUNT_WRONG);
        assertTrue(taskManager.getEpics().isEmpty(), TASKS_DESERIALIZED_COUNT_WRONG);
    }

}