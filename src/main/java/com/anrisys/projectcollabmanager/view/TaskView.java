package com.anrisys.projectcollabmanager.view;

import com.anrisys.projectcollabmanager.application.AppContext;
import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.CreateTaskWithEmailAssignee;
import com.anrisys.projectcollabmanager.dto.TaskDTO;
import com.anrisys.projectcollabmanager.dto.UpdateTaskRequest;
import com.anrisys.projectcollabmanager.entity.Task;
import com.anrisys.projectcollabmanager.service.TaskService;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.anrisys.projectcollabmanager.view.AuthView.emailRegexPattern;

public class TaskView {
    private final TaskService taskService;
    private final AppContext appContext;
    private Map<Integer, TaskDTO> allProjectTasks;
    private boolean isAllProjectTasksDirty;
    private Map<Integer, TaskDTO> myTasksList;
    private boolean isMyTasksListDirty;

    public TaskView(AppContext appContext, TaskService taskService) {
        this.appContext = appContext;
        this.taskService = taskService;
        this.allProjectTasks = new HashMap<>();
        this.myTasksList = new HashMap<>();
        this.isAllProjectTasksDirty = false;
    }

    public void create() {
        String title = taskTitlePrompt();
        String shortDescription = shortDescriptionPrompt();

        if (shortDescription.isEmpty()) shortDescription = null;

        if (appContext.getCurrentProjectState().isPersonal()) {
            CreateTaskRequest request = new CreateTaskRequest(
                    title,
                    shortDescription,
                    appContext.getCurrentProjectState().id(),
                    appContext.getCurrentUser().getId());
            taskService.create(request, appContext.getCurrentUser().getId());
        } else {
            String assigneeEmail = promptEmailUser();

            CreateTaskWithEmailAssignee request = new CreateTaskWithEmailAssignee(
                    title,
                    shortDescription,
                    appContext.getCurrentProjectState().id(),
                    assigneeEmail);
            taskService.createWithEmailAssignee(request, appContext.getCurrentUser().getId());
        }

        System.out.println("Successful create new task");
    }

    public void listTask() {
        if (isAllProjectTasksDirty) {
            List<TaskDTO> allTaskByProjectId = taskService.getAllTaskByProjectId(
                    appContext.getCurrentProjectState().id(),
                    appContext.getCurrentUser().getId()
            );

            for (int i = 0; i < allTaskByProjectId.size(); i++) {
                allProjectTasks.put(i + 1, allTaskByProjectId.get(i));
            }
        }

        printTasksList(allProjectTasks);
    }

    public void myTaskList() {
        if (isMyTasksListDirty) {
            List<TaskDTO> allTaskByProjectId = taskService.getAllTaskByProjectIdAndAssigneeId(
                    appContext.getCurrentProjectState().id(),
                    appContext.getCurrentUser().getId()
            );

            for (int i = 0; i < allTaskByProjectId.size(); i++) {
                allProjectTasks.put(i + 1, allTaskByProjectId.get(i));
            }
        }

        printTasksList(myTasksList);
    }

    public void showTask() {
        Integer projectIndex = taskIdxPrompt("x");

        Task task = taskService.getTaskById(allProjectTasks.get(projectIndex).id(), appContext.getCurrentUser().getId());

        printTask(task);
    }

    public void deleteTask() {
        Integer projectIndex = taskIdxPrompt("x");

        Task task = taskService.deleteById(allProjectTasks.get(projectIndex).id(), appContext.getCurrentUser().getId());

        isAllProjectTasksDirty = true;
        isMyTasksListDirty = true;
        System.out.println("Success delete task with title : " + task.getTitle());
    }

    public void changeTaskStatus() {
        Integer projectIndex = taskIdxPrompt("personal");
        TaskDTO taskDTO = myTasksList.get(projectIndex);
        Task.Status status = promptTaskStatus(taskDTO);

        Task task = taskService.updateStatus(taskDTO.id(), appContext.getCurrentUser().getId(), status);
        isAllProjectTasksDirty = true;
        isMyTasksListDirty = true;
        System.out.println("Successful update task status as : " + task.getStatus().name());
    }

    public void updateTask() {
        Integer projectIndex = taskIdxPrompt("x");
        TaskDTO taskDTO = myTasksList.get(projectIndex);

        String title = taskTitlePrompt();
        String shortDescription = shortDescriptionPrompt();
        if (shortDescription.isEmpty()) shortDescription = null;

        UpdateTaskRequest request = new UpdateTaskRequest(title, shortDescription);

        Task updated = taskService.update(taskDTO.id(), appContext.getCurrentUser().getId(), request);

        isAllProjectTasksDirty = true;
        isMyTasksListDirty = true;
        System.out.println("Successful updated task with title : " + updated.getTitle());
    }

    public void findTask() {}

    public void changeTaskAssignee() {
        Integer taskIdx = taskIdxPrompt("x");

        TaskDTO taskDTO = allProjectTasks.get(taskIdx);

        String assigneeEmail = promptEmailUser();

        Task changedAssignee = taskService.changeAssignee(taskDTO.id(), appContext.getCurrentUser().getId(), assigneeEmail);

        System.out.println("Successful change assignee of task : " + changedAssignee.getTitle());
    }

    private Task.Status promptTaskStatus(TaskDTO taskDTO) {
        while (true) {
            System.out.println("""
                    Choose task new status:\s
                    1. TODO
                    2. IN_PROGRESS
                    3. DONE
                   \s""");
            int input = CLIInputUtil.requestIntInput();
            switch (input) {
                case 1 : return Task.Status.TODO;
                case 2 : return Task.Status.IN_PROGRESS;
                case 3 : return Task.Status.DONE;
                default : System.out.println("Invalid input, please choose valid status");
            }
        }
    }

    private void printTasksList(Map<Integer, TaskDTO> tasks) {
        tasks.forEach(((integer, taskDTO) ->
                System.out.printf(
                        "%d. %s %s.%n%n", integer,
                        taskDTO.title(),
                        taskDTO.status()
                )));
    }

    private void printTask(Task task) {
        System.out.printf(
                "Title: %s.%nDescription: %s%nStatus: %s",
                task.getTitle(),
                task.getShortDescription(),
                task.getStatus().name()
        );
    }

    private String promptEmailUser() {
        while(true) {
            System.out.println("Email:");
            String email = CLIInputUtil.requestStringInput();
            if(email.matches(emailRegexPattern)) return email;
            System.out.println("Invalid email format");
        }
    }

    private String taskTitlePrompt() {
        while(true) {
            System.out.println("Task title:");
            System.out.println("To cancel action type: X");
            String input = CLIInputUtil.requestStringInput();
            if (!input.trim().isEmpty()) return input;
            System.out.println("Invalid project title.");
        }
    }

    private String shortDescriptionPrompt() {
        System.out.println("Task Description (optional):");
        return CLIInputUtil.requestStringInput();
    }

    private Integer taskIdxPrompt(String listType) {
        int listSize;
        if (Objects.equals(listType, "personal")) {
            listSize = myTasksList.size();
        } else {
            listSize = allProjectTasks.size();
        }
        while (true) {
            System.out.println("Choose project index: ");
            int idx = CLIInputUtil.requestIntInput();

            if (idx > 0 && idx <= listSize) {
                return idx;
            }

            System.out.println("Invalid project index.");
        }
    }

}
