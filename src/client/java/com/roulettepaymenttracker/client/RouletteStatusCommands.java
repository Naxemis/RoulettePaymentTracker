package com.roulettepaymenttracker.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class RouletteStatusCommands {

    private static final ActionBarNotification actionBarNotification = new ActionBarNotification();
    private static final PlaySoundEffect playSoundEffect = new PlaySoundEffect();
    private final AtomicBoolean rouletteStatus = new AtomicBoolean(false);

    private static final Gson gson = new Gson();
    private static final String filePath = System.getenv("APPDATA") + "/RoulettePaymentTracker/rouletteStatus.json";
    private static final Path statusFilePath = Paths.get(filePath);
    private void saveSatusToJSON() {
        try {
            if (Files.exists(statusFilePath.getParent())) { // created the directory if it's not existing
                Files.createDirectories(statusFilePath.getParent());
            }
            else {
                System.out.println("Roulette status file not found, loading defaults.");
                try {
                    System.out.println("Creating directories for rouletteStatus.json file.");
                    Files.createDirectories(statusFilePath.getParent());

                    try {
                        System.out.println("Creating empty rouletteStatus.json file.");
                        String defaultJson = "";
                        Files.write(statusFilePath, defaultJson.getBytes());
                        System.out.println("Created empty rouletteStatus.json file.");
                    } catch (IOException exception) {
                        System.out.println("Failed to create empty rouletteStatus.json file: " + exception.getMessage());
                    }
                } catch (IOException exception) {
                    System.out.println("Failed to create directories for rouletteStatus.json file: " + exception.getMessage());
                }
            }

            JsonObject json = new JsonObject();
            json.addProperty("rouletteStatus", rouletteStatus.get());

            try (FileWriter fileWriter = new FileWriter(filePath)) {
                gson.toJson(json, fileWriter);
                System.out.println("Successfully saved roulette status: " + rouletteStatus.get());
                actionBarNotification.sendMessage("Saved roulette status.", "§a");
                playSoundEffect.playSound(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER);
            }

        } catch (IOException exepction) {
            System.err.println("Failed to save roulette status: " + exepction.getMessage());
            actionBarNotification.sendMessage("Failed to save roulette status.", "§4");
            playSoundEffect.playSound(SoundEvents.ENTITY_ITEM_BREAK);
        }
    }

    public void register() {
        String alreadyChangedText = "§eRoulette status is already changed to: ";

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("roulette")
                            .then(literal("status")
                                .then(literal("start")
                                        .executes(context -> {
                                            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
                                                if (!rouletteStatus.get()) {
                                                    rouletteStatus.set(true);
                                                    saveSatusToJSON();
                                                    MinecraftClient.getInstance().player.sendMessage(Text.literal("§aRoulette status changed to: true."), false);
                                                }
                                                else {
                                                    MinecraftClient.getInstance().player.sendMessage(Text.literal(alreadyChangedText + rouletteStatus.get()), false);
                                                }
                                            }
                                            return 1;
                                        })
                                )
                                .then(literal("stop")
                                        .executes(context -> {
                                            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
                                                if (rouletteStatus.get()) {
                                                    rouletteStatus.set(false);
                                                    saveSatusToJSON();
                                                }
                                                else {
                                                    MinecraftClient.getInstance().player.sendMessage(Text.literal(alreadyChangedText + rouletteStatus.get()), false);
                                                }
                                            }

                                            return 1;
                                        })
                                )
                            )
            );
        });
    }

    public boolean getRouletteStatus() {
        return rouletteStatus.get();
    }

    public void reset_roulette_status() {
        rouletteStatus.set(false);
        saveSatusToJSON();
    }
}
