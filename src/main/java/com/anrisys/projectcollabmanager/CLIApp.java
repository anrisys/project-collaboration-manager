package com.anrisys.projectcollabmanager;

import com.anrisys.projectcollabmanager.application.CLIMenuManager;
import com.anrisys.projectcollabmanager.application.DBConfig;

public class CLIApp {
    public static void main(String[] args) {
        try {
            CLIMenuManager menuManager = new CLIMenuManager();
            menuManager.start();
        } finally {
            DBConfig.closeDataSource();
        }
    }
}
