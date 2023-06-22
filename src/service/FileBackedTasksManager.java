package service;
import model.*;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

public class FileBackedTasksManager extends InMemoryTaskManager {
    public final static Path FILEPATH = Paths.get(System.getProperty("user.home"), "dev", "java-kanban", "file.csv");
    private final static String TITLE = "id,type,name,status,description,epic";
    private final static String ENTER = "\n";
    private final Path path;

    public FileBackedTasksManager(Path path) {
        super();
        this.path = path;
    }

    public static void main(String[] args) {
        TaskManager manager = FileBackedTasksManager.loadFromFile(FILEPATH);

        Task task1 = new Task("T Загрузка проекта в репозиторий22"
                , "Запуште проект в репозиторий после коммита22", Task.TaskStatus.NEW);
        int taskId = manager.addNewTask(task1);

        int task_id = 0;
        String task_name = "";
        Task.TaskStatus task_status;
        manager.getTask(taskId);
        List<Task> taskList = manager.getHistoryManager().getHistory();
        for (Task task : taskList) {
            task_id = task.getId();
            task_name = task.getName();
            task_status = task.getStatus();
            System.out.printf("\nЗадача %d: %s. Задача в статусе %s", task_id, task_name, String.valueOf(task_status));
        }
    }

    private void save() throws ManagerSaveException {
        try (Writer fileWriter = new FileWriter(path.getFileName().toString())) {
            if (!Files.exists(path)) {
                Path file = Files.createFile(path);
            }
            fileWriter.write(TITLE + ENTER);
            super.getTasks().forEach((v) -> {
                try {
                    fileWriter.write(toString(v) + ENTER);
                } catch (IOException e) {
                    throw new ManagerSaveException();
                }
            });
            super.getEpics().forEach((v) -> {
                try {
                    fileWriter.write(toString(v) + ENTER);
                } catch (IOException e) {
                    throw new ManagerSaveException();
                }
            });
            super.getSubtasks().forEach((v) -> {
                try {
                    fileWriter.write(toString(v) + ENTER);
                } catch (IOException e) {
                    throw new ManagerSaveException();
                }
            });
            String historyStr = historyToString(super.getHistoryManager());
            if (!historyStr.isBlank()) {
                fileWriter.write(ENTER);
                fileWriter.write(historyStr);
            }
        } catch (IOException ioe) {
            throw new ManagerSaveException();
        }
    }

    public static FileBackedTasksManager loadFromFile(Path path) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(FILEPATH);
        try (BufferedReader fileReader = new BufferedReader(new FileReader(path.getFileName().toString()))) {
            List<String> linesList = new ArrayList<>();
            if (Files.exists(path)) {
                int linesCount = 0;
                while (fileReader.ready()) {
                    String line = fileReader.readLine();
                    ++linesCount;
                    if (!line.equals(TITLE) && !line.isBlank()) {
                        linesList.add(line);
                    }
                }
                String[] linesArray = linesList.toArray(new String[]{});
                List<Integer> historyList = new ArrayList<>();
                int dataLinesIndex = linesArray.length - 1;
                if (linesCount - linesArray.length > 1) {
                    historyList = historyFromString(linesArray[linesArray.length - 1]);
                    dataLinesIndex = linesArray.length - 2;
                }
                for (int i = 0; i <= dataLinesIndex; i++) {
                    Task taskFromFile = fileBackedTasksManager.fromString(linesArray[i]);
                }
                for (int taskIdFromHistory : historyList) {
                    Task taskFromHistory = new Task();
                    if (fileBackedTasksManager.getEpics().contains(fileBackedTasksManager.getEpic(taskIdFromHistory))) {
                        taskFromHistory = fileBackedTasksManager.getEpic(taskIdFromHistory);
                    } else if (fileBackedTasksManager.getSubtasks().contains(fileBackedTasksManager.getSubtask(taskIdFromHistory))) {
                        taskFromHistory = fileBackedTasksManager.getSubtask(taskIdFromHistory);
                    } else if (fileBackedTasksManager.getTasks().contains(fileBackedTasksManager.getTask(taskIdFromHistory))) {
                        taskFromHistory = fileBackedTasksManager.getTask(taskIdFromHistory);
                    }
                    if (taskFromHistory.getId() > 0) {
                        fileBackedTasksManager.getHistoryManager().add(taskFromHistory);
                    }
                }
            }
        } catch (IOException ioe) {
            System.out.println("IOE Exception");
        }
        return fileBackedTasksManager;
    }

    private String toString(Task task) {
        String className = task.getClass().toString().toUpperCase()
                               .replace("CLASS","").replace(" MODEL.", "");
        StringBuilder taskString = new StringBuilder()
                .append(task.getId()).append(",")
                .append(className).append(",")
                .append(task.getName()).append(",")
                .append(task.getStatus().toString()).append(",")
                .append(task.getDsc()).append(",");
        TaskType taskType = TaskType.valueOf(className);
        int epicId;
        if (taskType == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            epicId = subtask.getEpicId();
            taskString.append(epicId);
        } else {
            taskString.append(" ");
        }
        return taskString.toString();
    }

    private Task fromString(String value) {
        String[] elements = new String[5];
        elements = value.split(",");
        int id = Integer.parseInt(elements[0]);
        TaskType taskType = TaskType.valueOf(elements[1]);
        String taskName = elements[2];
        Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(elements[3]);
        String taskDsc = elements[4];
        int epicId = 0;
        if (!elements[5].isBlank()) {
            epicId = Integer.parseInt(elements[5]);
        }
        Task task = new Task();

        switch (taskType) {
            case EPIC:
                task = new Epic(taskName, taskDsc);
                task.setStatus(taskStatus);
                task.setId(id);
                addNewEpic((Epic) task);
                break;
            case SUBTASK:
                task = new Subtask(taskName, taskDsc, taskStatus, epicId);
                task.setId(id);
                addNewSubtask((Subtask) task);
                break;
            case TASK:
                task = new Task(taskName, taskDsc, taskStatus);
                task.setId(id);
                addNewTask(task);
                break;
        }
        return task;
    }

    private static String historyToString(HistoryManager manager) {
        List<Task> taskList = manager.getHistory();
        List<String> taskIdList = new ArrayList<>();
        taskList.forEach((v) -> {
            taskIdList.add(String.valueOf(v.getId()));
        });
        return String.join(",",taskIdList.toArray(new String[]{}));
    }

    private static List<Integer> historyFromString(String value) {
        String[] elements = value.split(",");
        List<Integer> historyFromStringList = new ArrayList<>();
        for (String element : elements) {
            int intElement = Integer.valueOf(element);
            historyFromStringList.add(intElement);
        }
        return historyFromStringList;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int subtaskId = super.addNewSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int epicId = super.addNewEpic(epic);
        save();
        return epicId;
    }

    @Override
    public int addNewTask(Task task) {
        int taskId = super.addNewTask(task);
        save();
        return taskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateTask(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateTask(subtask);
        save();
    }

    @Override
    public void deleteTask(int taskId) {
        super.deleteTask(taskId);
        save();
    }

    @Override
    public void deleteEpic(int epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        super.deleteSubtask(subtaskId);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public Task getTask(int taskId) {
        Task task = super.getTask(taskId);
        save();
        return task;
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        Subtask subtask = super.getSubtask(subtaskId);
        save();
        return subtask;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = super.getEpic(epicId);
        save();
        return epic;
    }

    private class ManagerSaveException extends RuntimeException {
        public ManagerSaveException() {
        }
    }
}
