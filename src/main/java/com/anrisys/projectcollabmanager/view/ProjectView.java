package com.anrisys.projectcollabmanager.view;

import com.anrisys.projectcollabmanager.application.ReadOnlyAppContext;
import com.anrisys.projectcollabmanager.dto.ProjectCreateRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
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
    private final Map<Integer, ProjectDTO> userProjects;
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

        ProjectCreateRequest project;
        if (description.isEmpty()) {
            project = new ProjectCreateRequest(title, context.getCurrentUser().getId(), true, null);
        } else {
            project = new ProjectCreateRequest(title, context.getCurrentUser().getId(), true, description);
        }
        isUserProjectsDirty = true;

        System.out.println("Successfully create project with title: " + project.title());
    }

    public void listProjects() {
        System.out.println("Your projects: ");
        if(isUserProjectsDirty) {
            List<ProjectDTO> projects = projectService.listPersonalProjects(context.getCurrentUser().getId());

            for (int i = 0; i < projects.size(); i++) {
                userProjects.put(i + 1, projects.get(i));
            }

            isUserProjectsDirty = false;
        } else {
            userProjects.forEach(this::printProjectDTO);
        }
    }

    public void showProject() {
        listProjects();

        int projectIndex = projectIndexPrompt();

        ProjectDTO project = userProjects.get(projectIndex);

        Project projectById = projectService.findPersonalProjectById(project.id(), context.getCurrentUser().getId());

        printProject(projectById);
    }

    public void updateProject() {
        listProjects();

        System.out.println("Choose project index you want to update: ");
        int projectIndex = projectIndexPrompt();

        ProjectDTO project = userProjects.get(projectIndex);
        try {
            String newTitle = titlePrompt();
            String newDescription = descriptionPrompt();
            ProjectUpdateRequest request = new ProjectUpdateRequest(newTitle, newDescription);

            projectService.updateProject(project.id(), context.getCurrentUser().getId(), request);
            isUserProjectsDirty = true;
        } catch (Exception e) {
            System.out.println("Can't update project. Please try again");
        }
    }

    public void deleteProject() {
        listProjects();

        System.out.println("Choose project index you want to delete: ");
        int projectIndex = projectIndexPrompt();

        ProjectDTO project = userProjects.get(projectIndex);
        try {
            projectService.deleteProject(project.id(), context.getCurrentUser().getId());
            isUserProjectsDirty = true;
        } catch (Exception e) {
            System.out.println("Can't delete project. Please try again");
        }
    }

    public void convertCollaborationProject() {
        int projectIndex = projectIndexPrompt();

        projectService.convertToCollaborative(userProjects.get(projectIndex).id(), context.getCurrentUser().getId());

        isUserProjectsDirty = true;

        System.out.println("Successful change project into a collaborative project");
    }

    public void revertIntoPersonalProject() {
        int projectIndex = projectIndexPrompt();

        projectService.revertToPersonal(userProjects.get(projectIndex).id(), context.getCurrentUser().getId());

        isUserProjectsDirty = true;

        System.out.println("Successful revert project into a personal project");
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

    private void printProjectDTO(int index , ProjectDTO project) {
        System.out.printf("%d. %s. %n", index, project.title());
    }

    private void printProject(Project project) {
        System.out.printf("Title: %s. %n Type: %s.%nDescription: %s.%n",
                project.getTitle(),
                project.isPersonal() ? "personal" : "collaborative",
                project.getDescription());
    }
}
