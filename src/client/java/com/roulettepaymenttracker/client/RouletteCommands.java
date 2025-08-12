package com.roulettepaymenttracker.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
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

public class RouletteCommands {

    private static final ActionBarNotification actionBarNotification = new ActionBarNotification();
    private static final PlaySoundEffect playSoundEffect = new PlaySoundEffect();
    private final AtomicBoolean rouletteStatus = new AtomicBoolean(false);
    private static final Gson gson = new Gson();
    private static final String filePath = System.getenv("APPDATA") + "/RoulettePaymentTracker/rouletteStatus.json";
    private static final Path statusFilePath = Paths.get(filePath);
    private void saveSatus() {
        try {
            if (!Files.exists(statusFilePath.getParent())) {
                Files.createDirectories(statusFilePath.getParent());
            }

            JsonObject json = new JsonObject();
            json.addProperty("rouletteActive", rouletteStatus.get());

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
            dispatcher.register(
                    ClientCommandManager.literal("roulette")
                            .then(ClientCommandManager.literal("start")
                                    .executes(context -> {
                                        if (!rouletteStatus.get()) {
                                            rouletteStatus.set(true);
                                            saveSatus();
                                            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
                                                MinecraftClient.getInstance().player.sendMessage(Text.literal("§aRoulette status changed to: true."), false);
                                            }
                                        } else {
                                            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
                                                MinecraftClient.getInstance().player.sendMessage(Text.literal(alreadyChangedText + rouletteStatus.get()), false);
                                            }
                                        }
                                        return 1;
                                    })
                            )
                            .then(ClientCommandManager.literal("stop")
                                    .executes(context -> {
                                        if (rouletteStatus.get()) {
                                            rouletteStatus.set(false);
                                            saveSatus();
                                            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
                                                MinecraftClient.getInstance().player.sendMessage(Text.literal("§4Roulette status changed to: false."), false);
                                            }
                                        } else {
                                            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
                                                MinecraftClient.getInstance().player.sendMessage(Text.literal(alreadyChangedText + rouletteStatus.get()), false);
                                            }
                                        }
                                        return 1;
                                    })
                            )
            );
        });
    }

    public void reset_roulette_status() {
        rouletteStatus.set(false);
        saveSatus();
    }
}
