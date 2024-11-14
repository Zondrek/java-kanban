package manager;

import manager.history.HistoryManager;
import manager.history.InMemoryHistoryManager;
import manager.task.FileBackedTaskManager;
import manager.task.TaskManager;

import java.io.File;
import java.io.IOException;

public class Managers {

    public static TaskManager getDefault() {
        try {
            File file = new File("test.scv");
            if (!file.exists()) {
                file.createNewFile();
            }
            return new FileBackedTaskManager(getDefaultHistory(), file);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
