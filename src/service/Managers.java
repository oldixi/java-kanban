package service;

public class Managers {

    public static TaskManager getDefault() {
        return (new HttpTaskManager(HttpTaskManager.URL));
    }

    public static HistoryManager getDefaultHistory() {
        return (new InMemoryHistoryManager());
    }
}