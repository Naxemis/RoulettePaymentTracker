package com.roulettepaymenttracker;

import java.io.File;

public class AppDataFolderCreator {
    public void createAppDataFolder() {
        String pathAppData = System.getenv("APPDATA");
        String folderName = "RoulettePaymentTracker";
        File folder = new File(pathAppData, folderName);

        if (!folder.exists()) {
            boolean createdFodler = folder.mkdirs();

            if (createdFodler) {
                System.out.println("AppData folder has been created.");
            }
            else {
                System.out.println("Something went wrong: Failed to create AppData folder.");
            }
        }
        else {
            System.out.println("AppData folder is already created.");
        }
    }
}
