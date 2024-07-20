package manager;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        if (file.exists()) {
            try (FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8)) {
                BufferedReader br = new BufferedReader(fileReader);
                br.readLine();
                while (br.ready()) {
                    String taskLine = br.readLine();
                    if (taskLine != null) {
                        fileBackedTaskManager.fromString(taskLine);
                    }
                }
            } catch (IOException exception) {
                throw new ManagerSaveException("Ошибка при чтении файла! " + exception.getMessage());
            }
        }
        return fileBackedTaskManager;
    }

    @Override
    public Task createTask(Task task) {
        task = super.createTask(task);
        save();
        return task;
    }

    @Override
    public void deleteAllTasks(Type type) {
        super.deleteAllTasks(type);
        save();
    }

    @Override
    public void deleteById(int id) {
        super.deleteById(id);
        save();
    }

    @Override
    public Task updateTask(Task task) {
        task = super.updateTask(task);
        save();
        return task;
    }

    private void save() {
        List<Task> tasks = getAll();
        try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
            fileWriter.write("id;type;name;description;status;epic\n");
            for (Task task : tasks) {
                fileWriter.write(task.toString() + "\n");
            }

        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка при сохранении задачи в файл! " + exception.getMessage());
        }
    }

    public Task fromString(String value) {

        String[] split = value.split(";");
        switch (split[1]) {
            case "TASK":
                Task task = new Task(Integer.parseInt(split[0]), split[2], split[3], convertStringToStatus(split[4]));
                super.createTask(task);
                return task;
            case "SUBTASK":
                Subtask subtask = new Subtask(Integer.parseInt(split[0]), split[2], split[3], convertStringToStatus(split[4]), (Epic) getById(Integer.parseInt(split[5])));
                super.createTask(subtask);
                return subtask;
            case "EPIC":
                Epic epic = new Epic(Integer.parseInt(split[0]), split[2], split[3], convertStringToStatus(split[4]));
                super.createTask(epic);
                return epic;
        }
        return null;
    }

    private Status convertStringToStatus(String stringStatus) {
        switch (stringStatus) {
            case "NEW":
                return Status.NEW;
            case "IN_PROGRESS":
                return Status.IN_PROGRESS;
            case "DONE":
                return Status.DONE;
        }
        return null;
    }
}
