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
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class FileBackedTasksManager extends InMemoryTaskManager {
    public static final Path FILEPATH = Paths.get(System.getProperty("user.home"), "dev", "java-kanban", "file.csv");
    public static final String TITLE = "id,type,name,status,description,epic,startTime,duration";
    private final Path path;

    public FileBackedTasksManager(Path path) {
        super();
        this.path = path;
    }

    private void save() throws ManagerSaveException {
        try (Writer fileWriter = new FileWriter(path.getFileName().toString())) {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            fileWriter.write(TITLE + "\n");
            super.getTasks().forEach((v) -> {
                try {
                    fileWriter.write(toString(v) + "\n");
                } catch (IOException e) {
                    throw new ManagerSaveException();
                }
            });
            super.getEpics().forEach((v) -> {
                try {
                    fileWriter.write(toString(v) + "\n");
                } catch (IOException e) {
                    throw new ManagerSaveException();
                }
            });
            super.getSubtasks().forEach((v) -> {
                try {
                    fileWriter.write(toString(v) + "\n");
                } catch (IOException e) {
                    throw new ManagerSaveException();
                }
            });
            String historyStr = historyToString(super.getHistoryManager());
            if (!historyStr.isBlank()) {
                fileWriter.write("\n" + historyStr);
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
                    fileBackedTasksManager.fromString(linesArray[i]);
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
            throw new ManagerSaveException();
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
            taskString.append(epicId).append(",");
        } else {
            taskString.append(" ,");
        }
        if (task.getStartTime() != null) {
            taskString.append(task.getStartTime().format(Task.FORMATTER)).append(",");
        } else {
            taskString.append(" ,");
        }
        if (task.getDuration() != 0) {
            taskString.append(task.getDuration());
        } else {
            taskString.append(" ,");
        }
        return taskString.toString();
    }

    private Task fromString(String value) {
        String[] elements = new String[7];
        elements = value.split(",");
        int id = Integer.parseInt(elements[0]);
        TaskType taskType = TaskType.valueOf(elements[1]);
        String taskName = elements[2];
        TaskStatus taskStatus = TaskStatus.valueOf(elements[3]);
        String taskDsc = elements[4];
        int epicId = 0;
        int duration = 0;
        LocalDateTime startTime = null;
        if (!elements[5].isBlank()) {
            epicId = Integer.parseInt(elements[5]);
        }
        if (!elements[6].isBlank()) {
            startTime = LocalDateTime.parse(elements[6], Task.FORMATTER);
        }
        if (!elements[7].isBlank()) {
            duration = Integer.parseInt(elements[7]);
        }
        Task task = new Task();

        switch (taskType) {
            case EPIC:
                task = new Epic(taskName, taskDsc);
                task.setStatus(taskStatus);
                task.setDuration(duration);
                task.setStartTime(startTime);
                task.setId(id);
                addNewEpic((Epic) task);
                break;
            case SUBTASK:
                task = new Subtask(taskName, taskDsc, taskStatus, epicId);
                task.setId(id);
                task.setDuration(duration);
                task.setStartTime(startTime);
                addNewSubtask((Subtask) task);
                break;
            case TASK:
                task = new Task(taskName, taskDsc, taskStatus, duration, startTime);
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
            int intElement = Integer.parseInt(element);
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
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
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
}
