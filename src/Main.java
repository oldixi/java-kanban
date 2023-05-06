import model.*;
import service.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ManagerForInterface manager = new ManagerForInterface();

        Task task1 = new Task("Скачивание проекта из репозитория"
                             ,"По пришедшему на почту приглашению клонируйте проект на локальный компьютер");
        Task task2 = new Task("Загрузка проекта в репозиторий"
                             ,"Запуште проект в репозиторий после коммита");

        Epic epic1 = new Epic("Проектирование проекта ФП-3", "Спроектируйте проект согласно ТЗ к ФП-3");

        Epic epic2 = new Epic("Разработка проекта ФП-3", "Разработайте проект согласно ТЗ к ФП-3");

        int taskId1 = manager.addNewTask (task1);
        int taskId2 = manager.addNewTask (task2);
        for (Task task : manager.getTasks()) {
            System.out.println(task.getId());;
        }

        int epicId1 = manager.addNewEpic(epic1);
        int epicId2 = manager.addNewEpic(epic2);
        Subtask subtask1 = new Subtask("Разработка системы хранения задач"
                ,"Реализуйте классы и коллекции хранения задач", "NEW", epicId2);
        Subtask subtask2 = new Subtask("Разработка логики управления задачами"
                ,"Реализуйте методы управления задачами", "NEW", epicId2);
        Subtask subtask3 = new Subtask("Проектирование модели ситемы"
                ,"Спроектируйте модель системы", "NEW", epicId1);
        int subtaskId1 = manager.addNewSubtask(subtask1);
        int subtaskId2 = manager.addNewSubtask(subtask2);
        ArrayList<Subtask> subtasks1 = new ArrayList<>();
        subtasks1.add(subtask1);
        subtasks1.add(subtask2);
        epic2.setSubtaskArray(subtasks1);
        manager.addNewEpic(epic2);

        int subtaskId3 = manager.addNewSubtask(subtask3);
        ArrayList<Subtask> subtasks2 = new ArrayList<>();
        subtasks2.add(subtask3);
        epic1.setSubtaskArray(subtasks2);
        manager.addNewEpic(epic1);

        ArrayList<Subtask> subtasks = manager.getEpicSubtasks(epicId2);
        if (subtasks != null) {
            for (Subtask subtask : subtasks) {
                System.out.println(subtask.getId());
            }
        }

    }
}
