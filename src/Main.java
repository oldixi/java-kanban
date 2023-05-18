import model.*;
import service.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager inMemoryTaskManager = Managers.getDefault();
        HistoryManager inMemoryHistoryManager = Managers.getDefaultHistory();

        Task task1 = new Task("Скачивание проекта из репозитория"
                             ,"По пришедшему на почту приглашению клонируйте проект на локальный компьютер"
                             ,Task.TaskStatus.NEW);
        Task task2 = new Task("Загрузка проекта в репозиторий"
                             ,"Запуште проект в репозиторий после коммита", Task.TaskStatus.DONE);

        int taskId1 = inMemoryTaskManager.addNewTask (task1);
        int taskId2 = inMemoryTaskManager.addNewTask (task2);
        for (Task task : inMemoryTaskManager.getTasks()) {
            System.out.println(task.getId());;
        }

        ArrayList<Subtask> subtasks1 = new ArrayList<>();
        ArrayList<Subtask> subtasks2 = new ArrayList<>();
        Epic epic1 = new Epic("Проектирование проекта ФП-3", "Спроектируйте проект согласно ТЗ к ФП-3", subtasks2);
        Epic epic2 = new Epic("Разработка проекта ФП-3", "Разработайте проект согласно ТЗ к ФП-3", subtasks1);

        int epicId2 = inMemoryTaskManager.addNewEpic(epic1);
        int epicId1 = inMemoryTaskManager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Разработка системы хранения задач"
                ,"Реализуйте классы и коллекции хранения задач", Task.TaskStatus.NEW, epicId2);
        Subtask subtask2 = new Subtask("Разработка логики управления задачами"
                ,"Реализуйте методы управления задачами", Task.TaskStatus.NEW, epicId2);
        Subtask subtask3 = new Subtask("Проектирование модели ситемы"
                ,"Спроектируйте модель системы", Task.TaskStatus.NEW, epicId1);
        Subtask subtask4 = new Subtask("Проектирование модели ситемы2"
                ,"Спроектируйте модель системы2", Task.TaskStatus.NEW, epicId1);
        Subtask subtask5 = new Subtask("Проектирование модели ситемы3"
                ,"Спроектируйте модель системы3", Task.TaskStatus.NEW, epicId1);
        Subtask subtask6 = new Subtask("Проектирование модели ситемы4"
                ,"Спроектируйте модель системы4", Task.TaskStatus.NEW, epicId1);

        boolean isEqualsSubtasks = subtask3.equals(subtask4);
        System.out.println(isEqualsSubtasks);

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
        subtask6.setStatus(Task.TaskStatus.IN_PROGRESS);
        inMemoryTaskManager.updateSubtask(subtask6);

        ArrayList<Subtask> subtasks = inMemoryTaskManager.getEpicSubtasks(epicId2);
        if (subtasks != null) {
            for (Subtask subtask : subtasks) {
                System.out.println(subtask.getId());
            }
        }

        inMemoryTaskManager.getTask(taskId1);
        inMemoryTaskManager.getTask(taskId2);
        inMemoryTaskManager.getEpic(epicId1);
        inMemoryTaskManager.getEpic(epicId2);
        inMemoryTaskManager.getSubtask(subtaskId1);
        inMemoryTaskManager.getSubtask(subtaskId2);
        inMemoryTaskManager.getSubtask(subtaskId3);
        inMemoryTaskManager.getSubtask(subtaskId4);
        inMemoryTaskManager.getSubtask(subtaskId5);
        inMemoryTaskManager.getSubtask(subtaskId6);
        inMemoryTaskManager.getTask(taskId1);
        inMemoryTaskManager.getEpic(epicId1);

        List<Task> taskList = inMemoryHistoryManager.getHistory();
        int task_id = 0;
        String task_name = "";
        Task.TaskStatus task_status;
        for (Task task : taskList) {
            task_id = task.getId();
            task_name = task.getName();
            task_status = task.getStatus();
            System.out.printf("\nЗадача %s: %s. Задача в статусе %s", task_id, task_name, String.valueOf(task_status));
        }

    }
}
