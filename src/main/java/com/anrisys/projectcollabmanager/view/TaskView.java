package com.anrisys.projectcollabmanager.view;

import com.anrisys.projectcollabmanager.application.AppContext;
import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.CreateTaskWithEmailAssignee;
import com.anrisys.projectcollabmanager.dto.TaskDTO;
import com.anrisys.projectcollabmanager.entity.Task;
import com.anrisys.projectcollabmanager.service.TaskService;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.anrisys.projectcollabmanager.view.AuthView.emailRegexPattern;

public class TaskView {
    private final TaskService taskService;
    private final AppContext appContext;
    private Map<Integer, TaskDTO> tasksList;
    private boolean isTasksListDirty;

    public TaskView(AppContext appContext, TaskService taskService) {
        this.appContext = appContext;
        this.taskService = taskService;
        this.tasksList = new HashMap<>();
        this.isTasksListDirty = false;
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
        if (isTasksListDirty) {
            List<TaskDTO> allTaskByProjectId = taskService.getAllTaskByProjectId(
                    appContext.getCurrentProjectState().id(),
                    appContext.getCurrentUser().getId()
            );

            for (int i = 0; i < allTaskByProjectId.size(); i++) {
                tasksList.put(i + 1, allTaskByProjectId.get(i));
            }
        }

        printTasksList();
    }

    public void showTask() {
        Integer projectIndex = projectIdxPrompt();

        Task task = taskService.getTaskById(tasksList.get(projectIndex).id(), appContext.getCurrentUser().getId());

        printTask(task);
    }

    private void printTasksList() {
        tasksList.forEach(((integer, taskDTO) ->
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

    private Integer projectIdxPrompt() {
        listTask();
        while (true) {
            System.out.println("Choose project index: ");
            int idx = CLIInputUtil.requestIntInput();

            if (idx > 0 && idx <= tasksList.size()) {
                return idx;
            }

            System.out.println("Invalid project index.");
        }
    }

}
