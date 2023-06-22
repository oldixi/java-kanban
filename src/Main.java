import model.*;
import service.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager inMemoryTaskManager = Managers.getDefault();
        HistoryManager inMemoryHistoryManager = inMemoryTaskManager.getHistoryManager();

        Task task1 = new Task("T Скачивание проекта из репозитория"
                             ,"По пришедшему на почту приглашению клонируйте проект на локальный компьютер"
                             ,TaskStatus.NEW);
        Task task2 = new Task("T Загрузка проекта в репозиторий"
                             ,"Запуште проект в репозиторий после коммита", TaskStatus.DONE);
        Task task3 = new Task("T Загрузка проекта в репозиторий3"
                ,"Запуште проект в репозиторий после коммита3", TaskStatus.DONE);

        int taskId1 = inMemoryTaskManager.addNewTask(task1);
        int taskId2 = inMemoryTaskManager.addNewTask(task2);
        int taskId3 = inMemoryTaskManager.addNewTask(task3);

        ArrayList<Subtask> subtasks1 = new ArrayList<>();
        ArrayList<Subtask> subtasks2 = new ArrayList<>();
        Epic epic1 = new Epic("E Проектирование проекта ФП-3", "Спроектируйте проект согласно ТЗ к ФП-3", subtasks2);
        Epic epic2 = new Epic("E Разработка проекта ФП-3", "Разработайте проект согласно ТЗ к ФП-3", subtasks1);

        int epicId2 = inMemoryTaskManager.addNewEpic(epic1);
        int epicId1 = inMemoryTaskManager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("S Разработка системы хранения задач"
                ,"Реализуйте классы и коллекции хранения задач", TaskStatus.NEW, epicId2);
        Subtask subtask2 = new Subtask("S Разработка логики управления задачами"
                ,"Реализуйте методы управления задачами", TaskStatus.NEW, epicId2);
        Subtask subtask3 = new Subtask("S Проектирование модели ситемы"
                ,"Спроектируйте модель системы", TaskStatus.NEW, epicId1);
        Subtask subtask4 = new Subtask("S Проектирование модели ситемы2"
                ,"Спроектируйте модель системы2", TaskStatus.NEW, epicId1);
        Subtask subtask5 = new Subtask("S Проектирование модели ситемы3"
                ,"Спроектируйте модель системы3", TaskStatus.NEW, epicId1);
        Subtask subtask6 = new Subtask("S Проектирование модели ситемы4"
                ,"Спроектируйте модель системы4", TaskStatus.NEW, epicId1);

        boolean isEqualsSubtasks = subtask3.equals(subtask4);
        //System.out.println(isEqualsSubtasks);

        int subtaskId1 = inMemoryTaskManager.addNewSubtask(subtask1);
        int subtaskId2 = inMemoryTaskManager.addNewSubtask(subtask2);
        int subtaskId3 = inMemoryTaskManager.addNewSubtask(subtask3);
        int subtaskId4 = inMemoryTaskManager.addNewSubtask(subtask4);
        int subtaskId5 = inMemoryTaskManager.addNewSubtask(subtask5);
        int subtaskId6 = inMemoryTaskManager.addNewSubtask(subtask6);
        subtasks1.add(subtask1);
        subtasks1.add(subtask2);
        subtasks2.add(subtask3);
        subtasks2.add(subtask4);
        subtasks2.add(subtask5);
        subtasks2.add(subtask6);

        epic1.setSubtaskArray(subtasks2);
        inMemoryTaskManager.updateEpic(epic1);
        subtask6.setStatus(TaskStatus.IN_PROGRESS);
        inMemoryTaskManager.updateSubtask(subtask6);

        List<Subtask> subtasks = inMemoryTaskManager.getEpicSubtasks(epicId2);

        inMemoryTaskManager.getTask(taskId1);
        inMemoryTaskManager.getTask(taskId2);
        inMemoryTaskManager.getTask(taskId3);
        inMemoryTaskManager.getEpic(epicId2);
        inMemoryTaskManager.getSubtask(subtaskId3);
        inMemoryTaskManager.getSubtask(subtaskId4);
        inMemoryTaskManager.getSubtask(subtaskId5);
        inMemoryTaskManager.getSubtask(subtaskId6);

        List<Task> taskList = inMemoryHistoryManager.getHistory();
        int task_id = 0;
        String task_name = "";
        TaskStatus task_status;
        for (Task task : taskList) {
            task_id = task.getId();
            task_name = task.getName();
            task_status = task.getStatus();
            System.out.printf("\nЗадача %d: %s. Задача в статусе %s", task_id, task_name, String.valueOf(task_status));
        }
        System.out.println("\n-----------------------------------------");
        inMemoryTaskManager.getTask(taskId1);
        taskList = inMemoryHistoryManager.getHistory();
        for (Task task : taskList) {
            task_id = task.getId();
            task_name = task.getName();
            task_status = task.getStatus();
            System.out.printf("\nЗадача %d: %s. Задача в статусе %s", task_id, task_name, String.valueOf(task_status));
        }

    }
}
