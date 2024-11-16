package manager.task;

import manager.Managers;

class InMemoryTaskManagerTest extends BaseTaskManagerTest {

    // Для тестирования базового функционала
    @Override
    protected TaskManager createInstance() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
    }
}