package manager.history;

import model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        history.add(task);
        if (history.size() > 10) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(history);
    }
}
