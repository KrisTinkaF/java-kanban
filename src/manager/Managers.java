package manager;

import model.CrossTimeException;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefaultBackup() throws CrossTimeException {
        return FileBackedTaskManager.loadFromFile(new File("src/resources/backupFile.csv"));
    }
}
