package manager.history;

import model.Task;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final HashMap<Integer, Node<Task>> history = new HashMap<>();

    private Node<Task> last;

    private static class Node<T> {
        Node<T> prev;
        Node<T> next;
        T value;

        public Node(Node<T> prev, Node<T> next, T value) {
            this.prev = prev;
            this.next = next;
            this.value = value;
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        putLast(task);
        history.put(task.getId(), last);
    }

    @Override
    public void remove(int id) {
        Node<Task> oldNode = history.remove(id);
        if (oldNode != null) {
            removeNode(oldNode);
        }
    }

    @Override
    public List<Task> getHistory() {
        return collectList();
    }


    private void putLast(Task task) {
        if (last == null) {
            last = new Node<>(null, null, task);
        } else {
            last.next = new Node<>(last, null, task);
            last = last.next;
        }
    }

    private void removeNode(Node<Task> node) {
        if (last == node) {
            last = last.prev;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        }
    }

    private List<Task> collectList() {
        LinkedList<Task> result = new LinkedList<>();
        for (Node<Task> node = last; node != null; node = node.prev) {
            result.addFirst(node.value);
        }
        return result;
    }
}
