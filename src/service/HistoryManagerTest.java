package service;

import model.Epic;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryManagerTest {
    private static final String TASKS_CNT_WRONG = "Неверное количество просмотренных задач.";
    private static final String HISTORY_IS_NOT_BLANK = "История не пустая.";

    TaskManager taskManager = new InMemoryTaskManager();
    HistoryManager historyManager = Managers.getDefaultHistory();

    Task task1 = new Task("Задача1", "Выполнить задачу 1");
    Task task2 = new Task("Задача2", "Выполнить задачу 2");
    Epic epic1 = new Epic("Эпик1", "Выполнить Эпик1");

    @Test
    void shouldAddTaskToHistory() {
        historyManager.add(task1);
        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, HISTORY_IS_NOT_BLANK);
        assertEquals(1, history.size(), HISTORY_IS_NOT_BLANK);
    }

    @Test
    void shouldAddRepeatedTaskToEndOfHistory() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewEpic(epic1);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic1);
        historyManager.add(task2);
        final List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), TASKS_CNT_WRONG);
        assertEquals(task2.getId(), history.size() - 1
                , "Порядок задач в истории не соответствует порадяку просмотра.");
    }

    @Test
    void shouldRemoveTaskFromHistoryFirst() {
        final int task1Id = taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewEpic(epic1);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic1);
        historyManager.add(task2);
        historyManager.remove(task1Id);

        final List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), TASKS_CNT_WRONG);
        assertNotEquals(historyManager.getHistory().get(0), task1
                , "Первый просмотр в истории не удален.");
    }

    @Test
    void shouldRemoveTaskFromHistoryLast() {
        taskManager.addNewTask(task1);
        final int task2Id = taskManager.addNewTask(task2);
        taskManager.addNewEpic(epic1);
        historyManager.add(task1);
        historyManager.add(epic1);
        historyManager.add(task2);
        historyManager.remove(task2Id);

        final List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), TASKS_CNT_WRONG);
        assertNotEquals(historyManager.getHistory().get(historyManager.getHistory().size() - 1), task2
                , "Последний просмотр в истории не удален.");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        final int epicId = taskManager.addNewEpic(epic1);
        historyManager.add(task1);
        historyManager.add(epic1);
        historyManager.add(task2);
        int taskIndex = historyManager.getHistory().indexOf(epic1);
        historyManager.remove(epicId);

        final List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), TASKS_CNT_WRONG);
        assertNotEquals(historyManager.getHistory().get(taskIndex), epic1
                , "Просмотр из истории не удален.");
    }

    @Test
    void shouldGetHistory() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewEpic(epic1);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic1);
        final List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), TASKS_CNT_WRONG);
    }

    @Test
    void shouldNotGetHistoryBlank() {
        final List<Task> history = historyManager.getHistory();

        assertEquals(0, history.size(), "История просмотров не пустая.");
    }
}