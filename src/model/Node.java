package model;

public class Node<Task> {
    public Task task;
    public Node<Task> prev;
    public Node<Task> next;

    public Node(Node<Task> prev, Task task, Node<Task> next) {
        this.task = task;
        this.next = next;
        this.prev = prev;
    }

    public Node() {
    }

/*    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode()) + ": Ссылка prev = " + prev + ". Ссылка next = " + next;
    }*/
}
