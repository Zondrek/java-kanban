package manager.task;

import manager.Managers;

class InMemoryTaskManagerTest extends BaseTaskManagerTest {

    @Override
    protected TaskManager createInstance() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
    }
}