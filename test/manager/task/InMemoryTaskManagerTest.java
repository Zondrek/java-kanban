package manager.task;

import manager.Managers;

class InMemoryTaskManagerTest extends BaseTaskManagerTest<InMemoryTaskManager> {

    // Для тестирования базового функционала
    @Override
    protected InMemoryTaskManager createInstance() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
    }
}