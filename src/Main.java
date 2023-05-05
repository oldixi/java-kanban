import model.*;
import service.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        Manager manager = new Manager();

        Task task1 = new Task("Скачивание проекта из репозитория"
                             ,"По пришедшему на почту приглашению клонируйте проект на локальный компьютер");
        Task task2 = new Task("Загрузка проекта в репозиторий"
                             ,"Запуште проект в репозиторий после коммита");

        Subtask subtask = new Subtask("Ознакомление с ТЗ к ФП-3","Ознакомьтесь с ТЗ к ФП-3");
        Epic epic1 = new Epic("Проектирование проекта ФП-3", "Спроектируйте проект согласно ТЗ к ФП-3");
        ArrayList<Subtask> subtasks1 = new ArrayList<>();
        subtasks1.add(subtask);

        Subtask subtask1 = new Subtask("Разработка системы хранения задач"
                                      ,"Реализуйте классы и коллекции хранения задач");
        Subtask subtask2 = new Subtask("Разработка логики управления задачами"
                                      ,"Реализуйте методы управления задачами");
        Epic epic2 = new Epic("Разработка проекта ФП-3", "Разработайте проект согласно ТЗ к ФП-3");
        ArrayList<Subtask> subtasks2 = new ArrayList<>();
        subtasks2.add(subtask1);
        subtasks2.add(subtask2);

        System.out.println("\nТС1 - создание задачи с некорректным типом");
        int taskId3 = manager.createTask(task1, 4);

        System.out.println("\nТС2 - создание обыкновенных задач");
        int taskId1 = manager.createTask(task1, Manager.TASK);
        int taskId2 = manager.createTask(task2, Manager.TASK);

        System.out.println("\nТС3 - распечатка обыкновенных задач");
        for (int taskId : manager.getAllTasks(Manager.TASK)) {
            manager.printTask(taskId, Manager.TASK);
        }

        System.out.println("\nТС4.1 - изменение статуса обыкновенной задачи");
        task1.setStatus(Manager.INPROGRESS);
        manager.updateTask(taskId1, task1, Manager.TASK);
        manager.printTask(taskId1, Manager.TASK);
        System.out.println("\nТС4.2 - изменение статуса обыкновенной задачи с несуществующим id");
        manager.updateTask(8, task1, Manager.TASK);

        System.out.println("\nТС5.1 - удаление обыкновенной задачи");
        manager.removeTask(taskId1, Manager.TASK);
        System.out.println("\nТС5.2 - удаление обыкновенной задачи с несуществующим id");
        manager.removeTask(8, Manager.TASK);
        System.out.println("\nТС5.3 - удаление всех обыкновенных задач");
        manager.removeAllTasks(Manager.TASK);

        System.out.println("\nТС6 - создание эпик с подзадачами");
        int epicId1 = manager.createEpicWithSubtasks(epic1, subtasks1);
        System.out.println("\nТС6.1 - распечатка только эпик");
        manager.printTask(epicId1, Manager.EPIC);

        System.out.println("\nТС 7 - распечатка эпик с подзадачами");
        for (int subtastId : manager.getAllSubtasksForEpic(epicId1).keySet()) {
            manager.printTask(subtastId, Manager.SUBTASK);
        }

        int epicId2 = manager.createEpicWithSubtasks(epic2, subtasks2);
        manager.printEpicWithSubtasks(epicId2);

        System.out.println("\nТС8.1 - изменение статуса одной подзадачи на DONE");
        subtask1.setStatus(Manager.DONE);
        manager.updateTask(7, subtask1, Manager.SUBTASK);
        manager.printEpicWithSubtasks(epicId2);
        System.out.println("\nТС8.2 - изменение статуса всех подзадач на DONE");
        subtask2.setStatus(Manager.DONE);
        manager.updateTask(8, subtask2, Manager.SUBTASK);
        manager.printEpicWithSubtasks(epicId2);

        System.out.println("\nТС9 - удаление подзадачи");
        manager.removeTask(7, Manager.SUBTASK);
        System.out.println("\nТС10 - удаление всех подзадач");
        manager.removeAllTasks(Manager.SUBTASK);
        manager.printEpicWithSubtasks(epicId2);

        System.out.println("\nТС11 - изменение статуса эпик без учета статуса подзадач");
        Epic epic3 = new Epic("Проектирование проекта ФП-3", "Спроектируйте проект согласно ТЗ к ФП-3");
        epic3.setStatus(Manager.INPROGRESS);
        manager.updateTask(4, epic3, Manager.EPIC);

        System.out.println("\nТС12 - удаление эпик");
        int subtask6 = manager.createTask(subtask1, Manager.SUBTASK);
        int subtask7 = manager.createTask(subtask2, Manager.SUBTASK);
        subtask1.setStatus(Manager.NEW);
        subtask2.setStatus(Manager.NEW);
        subtask1.setEpicId(epicId1);
        subtask2.setEpicId(epicId1);
        manager.updateTask(subtask6, subtask1, Manager.SUBTASK);
        manager.updateTask(subtask7, subtask2, Manager.SUBTASK);
        manager.printEpicWithSubtasks(epicId1);
        manager.removeTask(epicId1, Manager.EPIC);
        manager.printEpicWithSubtasks(epicId1);
    }
}
