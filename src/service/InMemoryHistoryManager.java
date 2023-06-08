package service;

import model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> taskInHistoryMap = new HashMap<>();
    private final CustomLinkedList<Task> customTasksHistoryList = new CustomLinkedList<>();

    private class CustomLinkedList<T extends Task> {
        private Node<Task> first = null;
        private Node<Task> last = null;

        private Node<Task> linkLast(Task task) {
            Node<Task> prev = last;
            final Node<Task> newNode = new Node<>(prev, task, null);
            if (prev == null) {
                first = newNode;
            } else {
                prev.next = newNode;
            }
            last = newNode;
            return newNode;
        }

        private List<Task> getTasks() {
            List<Task> taskList = new ArrayList<>();
            Node<Task> node = customTasksHistoryList.first;
            while (node != null) {
                taskList.add(node.task);
                node = node.next;
            }
            return taskList;
        }

        private void removeNode(Node<Task> node) {
            Node<Task> prevNode = node.prev;
            Node<Task> nextNode = node.next;

            if (nextNode != null) {
                nextNode.prev = node.prev;
            }
            if (prevNode != null) {
                prevNode.next = node.next;
            }

            if (first == node) {
                first = nextNode;
            }
            if (last == node) {
                last = prevNode;
            }
        }
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            if (taskInHistoryMap.containsKey(task.getId())) {
                remove(task.getId());
            }
            taskInHistoryMap.put(task.getId(), customTasksHistoryList.linkLast(task));
        }
    }

    @Override
    public void remove(int id) {
        Node<Task> node = taskInHistoryMap.get(id);
        if (node != null) {
            Node<Task> prevNode = node.prev;
            Node<Task> nextNode = node.next;

            taskInHistoryMap.remove(id);
            customTasksHistoryList.removeNode(node);

            if (nextNode != null) {
                taskInHistoryMap.put(nextNode.task.getId(), node.next);
            }
            if (prevNode != null) {
                taskInHistoryMap.put(prevNode.task.getId(), node.prev);
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return customTasksHistoryList.getTasks();
    }
}