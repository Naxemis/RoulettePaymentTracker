package com.roulettepaymenttracker;

import net.fabricmc.api.ModInitializer;

public class RoulettePaymentTracker implements ModInitializer {

    AppDataFolderCreator appDataFolderCreator = new AppDataFolderCreator();

    @Override
    public void onInitialize() {
        appDataFolderCreator.createAppDataFolder();
    }
}
