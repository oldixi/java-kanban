package service;

public class Managers {

    public static TaskManager getDefault() {
        return (new FileBackedTasksManager(FileBackedTasksManager.FILEPATH));
    }

    public static HistoryManager getDefaultHistory() {
        return (new InMemoryHistoryManager());
    }
}