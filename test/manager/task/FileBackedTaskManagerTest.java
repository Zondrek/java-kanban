package manager.task;

import java.io.File;
import java.io.IOException;

class FileBackedTaskManagerTest extends BaseTaskManagerTest {

    @Override
    protected TaskManager createInstance() {
        try {
            return FileBackedTaskManager.loadFromFile(File.createTempFile("test", ".csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}