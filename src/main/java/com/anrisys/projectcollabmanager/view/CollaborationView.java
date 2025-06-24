package com.anrisys.projectcollabmanager.view;

import com.anrisys.projectcollabmanager.application.AppContext;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.service.CollaborationService;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollaborationView {
    private final CollaborationService collaborationService;
    private final AppContext context;
    private final Map<Integer, ProjectDTO> projectCollaborations;
    private boolean isProjectCollaborationsDirty;

    public CollaborationView(CollaborationService collaborationService, AppContext context) {
        this.collaborationService = collaborationService;
        this.context = context;
        this.projectCollaborations = new HashMap<>();
        this.isProjectCollaborationsDirty = false;
    }

    public void listMyCollaborations() {
        System.out.println("Your collaboration projects: ");
        if (this.isProjectCollaborationsDirty) {
            List<ProjectDTO> projectDTOS = collaborationService.listCollaborationProjects(
                    context.getCurrentUser().getId()
            );

            for (int i = 0; i < projectDTOS.size(); i++) {
                projectCollaborations.put(i, projectDTOS.get(i));
            }

            isProjectCollaborationsDirty = false;
        } else {
            projectCollaborations.forEach(this::printCollaborationProject);
        }
    }

    public void showCollaborationProject() {
        System.out.println("Choose project index: ");

        int projectIndex = projectCollaborationIndexPrompt();
        Project project = collaborationService.showCollaborationProject(projectCollaborations.get(projectIndex).id(), context.getCurrentUser().getId());
        printProjectDetail(project);
    }

    public void addUserToProjectCollaboration() {
        listMyCollaborations();

        System.out.println("Please enter project index: ");
        Integer projectIndex = projectCollaborationIndexPrompt();
        String email = promptEmail();

        collaborationService.inviteUserToProjectByEmail(projectCollaborations.get(projectIndex).id(), context.getCurrentUser().getId(), email);

        System.out.printf("Successful add user with email %s to project collaboration%n", email);
    }

    public void leaveProject() {
        listMyCollaborations();

        System.out.println("Please enter project index: ");
        Integer projectIndex = projectCollaborationIndexPrompt();

        collaborationService.leaveProjectCollaboration(projectCollaborations.get(projectIndex).id(), context.getCurrentUser().getId());

        System.out.printf("Successful add user with email %s to project collaboration%n",
                projectCollaborations.get(projectIndex).title()
        );
    }

    public void goToProject() {
        listMyCollaborations();

        int projectIndex = projectIndexPrompt();

        ProjectDTO projectDTO = projectCollaborations.get(projectIndex);

        context.setCurrentProjectState(projectDTO);
    }

    public void listProjectMembers() {
        listMyCollaborations();

        System.out.println("Please enter project index: ");
        int projectIndex = projectCollaborationIndexPrompt();

        List<UserDTO> userDTOS = collaborationService.listProjectMembers(projectCollaborations.get(projectIndex).id());

        for (int i = 0; i < userDTOS.size(); i++) {
            printProjectMember(i, userDTOS.get(i));
        }
    }

    public void removeUserFromProject() {
        listMyCollaborations();

        System.out.println("Please enter project index: ");
        Integer projectIndex = projectCollaborationIndexPrompt();
        String email = promptEmail();

        collaborationService.removeUserFromProjectByUserEmail(projectCollaborations.get(projectIndex).id(), context.getCurrentUser().getId(), email);

        System.out.printf("Successful remove user with email %s from project collaboration%n", email);
    }

    private int projectCollaborationIndexPrompt() {
        while (true) {
            int index = CLIInputUtil.requestIntInput();

            if(index > 0 && index <= projectCollaborations.size()) {
                return index;
            }

            System.out.println("Invalid project index");
        }
    }

    private String promptEmail() {
        while(true) {
            System.out.println("Email:");
            String email = CLIInputUtil.requestStringInput();
            if(email.matches(AuthView.emailRegexPattern)) return email;
            System.out.println("Invalid email format");
        }
    }

    private void printCollaborationProject(int index, ProjectDTO projectDTO) {
        System.out.printf("%d. %s. %n", index, projectDTO.title());
    }

    private void printProjectDetail(Project project) {
        System.out.printf("Title: %s.%nDescription: %s.",
                project.getTitle(), project.getDescription());
    }

    private void printProjectMember(int index, UserDTO userDTO) {
        System.out.printf("%d. %s. %n", index, userDTO.email() );
    }

    private int projectIndexPrompt() {
        while(true) {
            int index = CLIInputUtil.requestIntInput();

            if (index > 0 && index <= projectCollaborations.size()) {
                return index;
            }

            System.out.println("Invalid project index.");
        }
    }
}
