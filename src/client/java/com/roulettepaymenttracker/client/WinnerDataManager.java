package com.roulettepaymenttracker.client;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WinnerDataManager {

    private static final ActionBarNotification actionBarNotification = new ActionBarNotification();
    private static final PlaySoundEffect playSoundEffect = new PlaySoundEffect();

    private String username = "";
    private int amount = 0;
    private static final String filePath = System.getenv("APPDATA") + "/RoulettePaymentTracker/winnerData.json"; //file path to JSON file
    private static final Path winnerDataFilePath = Paths.get(filePath);

    static class WinnerData {
        String username;
        int amount;
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(1); // thread pool for database operations
    public CompletableFuture<Void> updateWinnerData() {
        return CompletableFuture.runAsync(() -> {
            try { // created the directory if it's not existing
                if (!Files.exists(winnerDataFilePath.getParent())) {
                    System.out.println("Creating directories for winnerData.json.");
                    Files.createDirectories(winnerDataFilePath.getParent());
                }
            } catch (Exception exception) {
                System.out.println("Failed to create directories for winnerData.json: " + exception.getMessage());
            }

            try {
                if (Files.size(winnerDataFilePath) == 0) {
                    return;
                }
            }
            catch (IOException exception) {
                // ignore
            }

            if (!Files.exists(winnerDataFilePath)) {
                System.out.println("Couldn't find winnerData.json file.");
                try {
                    System.out.println("Creating an empty winnerData.json file.");
                    String defaultJson = "";
                    Files.write(winnerDataFilePath, defaultJson.getBytes());
                    System.out.println("Created an empty winnerData.json file.");
                }
                catch (IOException exception) {
                    System.out.println("Failed to create empty winnerData.json file: " + exception.getMessage());
                    actionBarNotification.sendMessage("Failed to create winnerData.json.", "§4");
                    playSoundEffect.playSound(SoundEvents.ENTITY_ITEM_BREAK);
                }

                return;
            }

            Gson gson = new Gson();

            try (Reader reader = new FileReader(filePath)) {
                WinnerData winnerData = gson.fromJson(reader, WinnerData.class); // parse JSON

                if (winnerData != null) { // check for null to avoid crash
                    if (!winnerData.username.equals(username) || winnerData.amount != amount) {
                        this.username = winnerData.username;
                        this.amount = winnerData.amount;

                        System.out.println("Winner Username: " + this.username);
                        System.out.println("Winner Payment Amount: " + this.amount);

                        String command = String.format("pay %s %d", username, amount);

                        MinecraftClient minecraftClient = MinecraftClient.getInstance();

                        if (minecraftClient != null && minecraftClient.player != null) {
                            minecraftClient.player.networkHandler.sendChatCommand(command);

                            actionBarNotification.sendMessage(String.format("Winner: %s | Amount: %d$.", username, amount), "§a");
                            playSoundEffect.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
                        }

                    }
                } else {
                    this.username = "";
                    this.amount = 0;
                }
            }
            catch (IOException exception) {
                System.out.println("Filed to read data from winnerData JSON file: " + exception.getMessage());
                actionBarNotification.sendMessage("Failed to read winner data.", "§4");
                playSoundEffect.playSound(SoundEvents.ENTITY_ITEM_BREAK);
            }
        }, executorService);
    }

    public void async_process_shutdown() {
        executorService.shutdown(); // closes the database connection thread
        System.out.println("Closing Winner Data Manager thread.");
    }
}
