package com.roulettepaymenttracker.client;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WinnerDataManager {

    private static final ActionBarNotification actionBarNotification = new ActionBarNotification();
    private static final PlaySoundEffect playSoundEffect = new PlaySoundEffect();

    private String username = "";
    private int amount = 0;
    private static final String filePath = System.getenv("APPDATA") + "/RoulettePaymentTracker/winnerData.json"; //file path to JSON file

    static class WinnerData {
        String username;
        int amount;
    }


    private final ExecutorService executorService = Executors.newFixedThreadPool(1); // thread pool for database operations
    public CompletableFuture<Void> updateWinnerData() {
        return CompletableFuture.runAsync(() -> {
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

            } catch (IOException exception) {
                System.out.println("Couldn't read data from winnerData JSON file: " + exception.getMessage());
            }
        }, executorService);
    }

    public void async_process_shutdown() {
        executorService.shutdown(); // closes the database connection thread
        System.out.println("Closing Winner Data Manager thread.");
    }
}
