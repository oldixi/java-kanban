package service;

import java.io.IOException;

public class Managers {

    public static TaskManager getDefault() {
        return (new HttpTaskManager(HttpTaskManager.URL));
    }

    public static HistoryManager getDefaultHistory() {
        return (new InMemoryHistoryManager());
    }

    public static KVServer getDefaultKVServer() throws IOException {
        return new KVServer();
    }
}