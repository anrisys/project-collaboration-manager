package com.anrisys.projectcollabmanager.view;

import com.anrisys.projectcollabmanager.application.CLIMenuManager;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.service.ProjectService;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;

import java.util.Arrays;
import java.util.List;

public class ProjectView {
    private final ProjectService projectService;
    private final User user;
    // Maybe create a map with index start from 1 with value project

    public ProjectView(ProjectService projectService) {
        this.projectService = projectService;
        this.user = CLIMenuManager.getCurrentUser();
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
        System.out.println("Successfully create project with title: " + project.getTitle());
    }

    public void listProjects() {
        List<Project> projects = projectService.listProjectsByOwner(user.getId());

        System.out.println("Your projects: ");
        projects.forEach(this::printProject);
        for (int i = 1; i < projects.size(); i++) {
            Project project = projects.get(i - 1);
            System.out.printf("%d. %s. %n", i, project.getTitle());
        }
    }

    public void showProject() {
        listProjects();

        Long projectId = (long) projectIdPrompt();

        Project project = projectService.findProjectById(projectId, user.getId());

        printProject(project);
    }

    public void updateProject() {
        System.out.println("Choose project Id which you want to update: ");
        // Create array/list of valid id of the user
        listProjects();
        int projected = projectIdPrompt();
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

    private int projectIdPrompt() {
        System.out.println("Project id: ");
        return CLIInputUtil.requestIntInput();
    }

    private void printProject(Project project) {
        System.out.printf("%d. %s. %n", project.getId(), project.getTitle());
    }
}
