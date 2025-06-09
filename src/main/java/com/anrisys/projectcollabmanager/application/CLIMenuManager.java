package com.anrisys.projectcollabmanager.application;

public class CLIMenuManager {
    private final ViewRegistry viewRegistry;
    private AppContext context;

    public CLIMenuManager(ViewRegistry viewRegistry, AppContext context) {
        this.viewRegistry = viewRegistry;
        this.context = context;
    }

    public void start() {
        System.out.println("CLI menu start");
    }
}
