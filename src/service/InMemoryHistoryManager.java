package service;

import model.*;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final List<Task> tasksHistoryList = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (tasksHistoryList.size() >= 10) {
            tasksHistoryList.remove(0);
        }
        tasksHistoryList.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return tasksHistoryList;
    }
}