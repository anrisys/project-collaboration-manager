package com.anrisys.projectcollabmanager.view;

import com.anrisys.projectcollabmanager.application.CLIMenuManager;
import com.anrisys.projectcollabmanager.dto.ProjectUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.service.ProjectService;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectView {
    private final ProjectService projectService;
    private final User user;
    private final Map<Integer, Project> userProjects;
    private boolean isUserProjectsDirty;

    public ProjectView(ProjectService projectService) {
        this.projectService = projectService;
        this.user = CLIMenuManager.getCurrentUser();
        this.userProjects = new HashMap<>();
        isUserProjectsDirty = false;
    }

    public void create() {
        String title = titlePrompt();
        String description = descriptionPrompt();

        Project project;
        if (description.trim().isEmpty()) {
            project = Project.create(title, user.getId());
        } else {
            project = Project.createWithDescription(title, user.getId(), description);
        }
        isUserProjectsDirty = true;
        System.out.println("Successfully create project with title: " + project.getTitle());
    }

    public void listProjects() {
        System.out.println("Your projects: ");
        if(isUserProjectsDirty) {
            List<Project> projects = projectService.listProjectsByOwner(user.getId());

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

            projectService.updateProject(project.getId(), user.getId(), request);
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
            projectService.deleteProject(project.getId(), user.getId());
            isUserProjectsDirty = true;
        } catch (Exception e) {
            System.out.println("Can't delete project. Please try again");
        }
    }

    private String titlePrompt() {
        while(true) {
            System.out.println("Project title:");
            String title = CLIInputUtil.requestStringInput();
            if (!title.trim().isEmpty()) return title;
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
