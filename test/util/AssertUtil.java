package util;

import model.Epic;
import model.SubTask;
import model.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class AssertUtil {

    public static void assertEqualsTask(Task t1, Task t2) {
        assertEquals(t1.getType(), t2.getType());
        assertEquals(t1.getStatus(), t2.getStatus());
        assertEquals(t1.getName(), t2.getName());
        assertEquals(t1.getDescription(), t2.getDescription());
        assertEquals(t1.getStartTime(), t2.getStartTime());
        assertEquals(t1.getDuration(), t2.getDuration());
    }

    public static void assertEqualsSubTask(SubTask t1, SubTask t2) {
        assertEqualsTask(t1, t2);
        assertEquals(t1.getEpicId(), t2.getEpicId());
    }

    public static void assertEqualsEpic(Epic e1, Epic e2) {
        assertEqualsTask(e1, e2);
        assertIterableEquals(e1.getSubTasks(), e2.getSubTasks());
    }
}
