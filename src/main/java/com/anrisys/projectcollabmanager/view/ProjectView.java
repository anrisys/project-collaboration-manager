package com.anrisys.projectcollabmanager.view;

import com.anrisys.projectcollabmanager.application.ReadOnlyAppContext;
import com.anrisys.projectcollabmanager.dto.ProjectUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.service.ProjectService;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectView {
    private final ProjectService projectService;
    private final ReadOnlyAppContext context;
    private final Map<Integer, Project> userProjects;
    private boolean isUserProjectsDirty;

    public ProjectView(ProjectService projectService, ReadOnlyAppContext context) {
        this.projectService = projectService;
        this.context = context;
        this.userProjects = new HashMap<>();
        isUserProjectsDirty = false;
    }

    public void createProject() {
        String title = titlePrompt();

        String description = descriptionPrompt();

        Project project;
        if (description.trim().isEmpty()) {
            project = Project.create(title, context.getCurrentUser().getId());
        } else {
            project = Project.createWithDescription(title, context.getCurrentUser().getId(), description);
        }
        isUserProjectsDirty = true;
        System.out.println("Successfully create project with title: " + project.getTitle());
    }

    public void listProjects() {
        System.out.println("Your projects: ");
        if(isUserProjectsDirty) {
            List<Project> projects = projectService.listProjectsByOwner(context.getCurrentUser().getId());

            for (int i = 0; i < projects.size(); i++) {
                userProjects.put(i + 1, projects.get(i));
            }

            isUserProjectsDirty = false;
        } else {
            userProjects.forEach(this::printProject);
        }
    }

    public void showProject() {
        listProjects();

        int projectIndex = projectIndexPrompt();

        Project project = userProjects.get(projectIndex);

        printProject(projectIndex, project);
    }

    public void updateProject() {
        listProjects();

        System.out.println("Choose project index you want to update: ");
        int projectIndex = projectIndexPrompt();

        Project project = userProjects.get(projectIndex);
        try {
            String newTitle = titlePrompt();
            String newDescription = descriptionPrompt();
            ProjectUpdateRequest request = new ProjectUpdateRequest(newTitle, newDescription);

            projectService.updateProject(project.getId(), context.getCurrentUser().getId(), request);
            isUserProjectsDirty = true;
        } catch (Exception e) {
            System.out.println("Can't update project. Please try again");
        }
    }

    public void deleteProject() {
        listProjects();

        System.out.println("Choose project index you want to delete: ");
        int projectIndex = projectIndexPrompt();

        Project project = userProjects.get(projectIndex);
        try {
            projectService.deleteProject(project.getId(), context.getCurrentUser().getId());
            isUserProjectsDirty = true;
        } catch (Exception e) {
            System.out.println("Can't delete project. Please try again");
        }
    }

    private String titlePrompt() {
        while(true) {
            System.out.println("Project title:");
            System.out.println("To cancel action type: X");
            String input = CLIInputUtil.requestStringInput();
            if (!input.trim().isEmpty()) return input;
            System.out.println("Invalid project title.");
        }
    }

    private String descriptionPrompt() {
        System.out.println("Project Description (optional):");
        return CLIInputUtil.requestStringInput();
    }

    private int projectIndexPrompt() {
        while(true) {
            int index = CLIInputUtil.requestIntInput();

            if (index > 0 && index <= userProjects.size()) {
                return index;
            }

            System.out.println("Invalid project index.");
        }
    }

    private void printProject(int index ,Project project) {
        System.out.printf("%d. %s. %n", index, project.getTitle());
    }
}
