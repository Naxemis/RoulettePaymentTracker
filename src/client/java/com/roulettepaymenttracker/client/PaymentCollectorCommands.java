package com.roulettepaymenttracker.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
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

public class PaymentCollectorCommands {
    private static final ActionBarNotification actionBarNotification = new ActionBarNotification();
    private static final PlaySoundEffect playSoundEffect = new PlaySoundEffect();

    private static int positionOfSpecifiedWord = 1; // where's located first word that player want to use for checking
    private static String specifiedComponentWord = "Otrzymałeś:"; // the word that will be checked with payment message
    private static int positionOfAmount = 2; // where's located amount that player has sent
    private static int positionOfUsername = 4; // where's located player's username
    private static int paymentMessageComponentsSize = 5; // what's the size of the payment message
    public int getPositionOfSpecifiedWord() {
        return positionOfSpecifiedWord;
    }
    public String getSpecifiedComponentWord() {
        return specifiedComponentWord;
    }
    public int getPositionOfAmount() {
        return positionOfAmount;
    }
    public int getPositionOfUsername() {
        return positionOfUsername;
    }
    public int getPaymentMessageComponentsSize() {
        return paymentMessageComponentsSize;
    }

    private static final Gson gson = new Gson();
    private static final String filePath = System.getenv("APPDATA") + "/RoulettePaymentTracker/paymentCollectorConfig.json";
    private static final Path paymentCollectorConfigFilePath = Paths.get(filePath);

    public void saveConfigToJSON() {
        try {
            if (Files.exists(paymentCollectorConfigFilePath.getParent())) {
                JsonObject json = new JsonObject();
                json.addProperty("positionOfSpecifiedWord", positionOfSpecifiedWord);
                json.addProperty("specifiedComponentWord", specifiedComponentWord);
                json.addProperty("positionOfAmount", positionOfAmount);
                json.addProperty("positionOfUsername", positionOfUsername);
                json.addProperty("paymentMessageComponentsSize", paymentMessageComponentsSize);

                try (FileWriter fileWriter = new FileWriter(filePath)) {
                    gson.toJson(json, fileWriter);
                    System.out.println("Successfully saved payment collector config.");
                    actionBarNotification.sendMessage("Saved data to config.", "§a");
                    playSoundEffect.playSound(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER);
                }
            }
            else {
                System.out.println("Payment collector config file not found, loading defaults.");
                System.out.println("Creating directories for paymentCollectorConfig.json");
                Files.createDirectories(paymentCollectorConfigFilePath.getParent());
            }

        } catch (IOException exepction) {
            System.err.println("Failed to save payment collector config: " + exepction.getMessage());
            actionBarNotification.sendMessage("Failed to save data to config.", "§4");
            playSoundEffect.playSound(SoundEvents.ENTITY_ITEM_BREAK);
        }
    }

    public void loadConfigFromJSON() {
        try {
            if (Files.exists(paymentCollectorConfigFilePath)) {
                String jsonString = Files.readString(paymentCollectorConfigFilePath);
                JsonObject json = gson.fromJson(jsonString, JsonObject.class);

                if (json.has("positionOfSpecifiedComponent"))
                    positionOfSpecifiedWord = json.get("positionOfSpecifiedComponent").getAsInt();

                if (json.has("specifiedComponentWord"))
                    specifiedComponentWord = json.get("specifiedComponentWord").getAsString();

                if (json.has("positionOfAmount"))
                    positionOfAmount = json.get("positionOfAmount").getAsInt();

                if (json.has("positionOfUsername"))
                    positionOfUsername = json.get("positionOfUsername").getAsInt();

                if (json.has("paymentMessageComponentsSize"))
                    paymentMessageComponentsSize = json.get("paymentMessageComponentsSize").getAsInt();

                System.out.println("Successfully loaded payment collector config.");
            }
            else {
                System.out.println("Payment collector connfig file not found.");
                try {
                    System.out.println("Creating directories for paymentCollectorConfig.json file.");
                    Files.createDirectories(paymentCollectorConfigFilePath.getParent());

                    try {
                        System.out.println("Creating empty paymentCollectorConfig.json file.");
                        String defaultJson = "";
                        Files.write(paymentCollectorConfigFilePath, defaultJson.getBytes());
                        System.out.println("Created empty paymentCollectorConfig.json file.");
                    } catch (IOException exception) {
                        System.out.println("Failed to create empty paymentCollectorConfig.json file: " + exception.getMessage());
                    }
                } catch (IOException exception) {
                    System.out.println("Failed to create directories for paymentCollectorConfig.json file: " + exception.getMessage());
                }
            }

        } catch (IOException exception) {
            System.err.println("Failed to load payment collector config: " + exception.getMessage());
        }
    }

    public void register() {
        String alreadyChangedText = "§eValue is already changed to: ";

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("roulette")
                            .then(literal("collectorconfig")
                                    .then(literal("info")
                                            .executes(context -> {
                                                MinecraftClient client = MinecraftClient.getInstance();
                                                if (client != null && client.player != null) {
                                                    client.player.sendMessage(Text.literal("§6[---- Payment Collector Config Info ----]"), false);
                                                    client.player.sendMessage(Text.literal("§7Specified component word: §a" + specifiedComponentWord), false);
                                                    client.player.sendMessage(Text.literal("§7Position of specified word: §a" + positionOfSpecifiedWord), false);
                                                    client.player.sendMessage(Text.literal("§7Position of amount: §a" + positionOfAmount), false);
                                                    client.player.sendMessage(Text.literal("§7Position of username: §a" + positionOfUsername), false);
                                                    client.player.sendMessage(Text.literal("§7Message components size: §a" + paymentMessageComponentsSize), false);
                                                }
                                                return 1;
                                            })
                                    )
                                    .then(literal("set")
                                            // specified component word
                                            .then(literal("specifiedcomponentword")
                                                    .then(argument("word", StringArgumentType.word())
                                                            .executes(context -> {
                                                                String newWord = StringArgumentType.getString(context, "word");
                                                                MinecraftClient client = MinecraftClient.getInstance();
                                                                if (client != null && client.player != null) {
                                                                    if (!specifiedComponentWord.equals(newWord)) {
                                                                        specifiedComponentWord = newWord;
                                                                        saveConfigToJSON();
                                                                        client.player.sendMessage(Text.literal("§aSpecified word set to: " + specifiedComponentWord), false);
                                                                    } else {
                                                                        client.player.sendMessage(Text.literal(alreadyChangedText + specifiedComponentWord), false);
                                                                    }
                                                                }
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            // position of specified word
                                            .then(literal("positionofspecifiedword")
                                                    .then(argument("position", IntegerArgumentType.integer())
                                                            .executes(context -> {
                                                                int newPosition = IntegerArgumentType.getInteger(context, "position");
                                                                MinecraftClient client = MinecraftClient.getInstance();
                                                                if (client != null && client.player != null) {
                                                                    if (positionOfSpecifiedWord != newPosition) {
                                                                        positionOfSpecifiedWord = newPosition;
                                                                        saveConfigToJSON();
                                                                        client.player.sendMessage(Text.literal("§aPosition of specified word set to: " + positionOfSpecifiedWord), false);
                                                                    } else {
                                                                        client.player.sendMessage(Text.literal(alreadyChangedText + positionOfSpecifiedWord), false);
                                                                    }
                                                                }
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            // position of amount
                                            .then(literal("positionofamount")
                                                    .then(argument("position", IntegerArgumentType.integer())
                                                            .executes(context -> {
                                                                int newPosition = IntegerArgumentType.getInteger(context, "position");
                                                                MinecraftClient client = MinecraftClient.getInstance();
                                                                if (client != null && client.player != null) {
                                                                    if (positionOfAmount != newPosition) {
                                                                        positionOfAmount = newPosition;
                                                                        saveConfigToJSON();
                                                                        client.player.sendMessage(Text.literal("§aPosition of payment amount set to: " + positionOfAmount), false);
                                                                    } else {
                                                                        client.player.sendMessage(Text.literal(alreadyChangedText + positionOfAmount), false);
                                                                    }
                                                                }
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            // position of username
                                            .then(literal("positionofusername")
                                                    .then(argument("position", IntegerArgumentType.integer())
                                                            .executes(context -> {
                                                                int newPosition = IntegerArgumentType.getInteger(context, "position");
                                                                MinecraftClient client = MinecraftClient.getInstance();
                                                                if (client != null && client.player != null) {
                                                                    if (positionOfUsername != newPosition) {
                                                                        positionOfUsername = newPosition;
                                                                        saveConfigToJSON();
                                                                        client.player.sendMessage(Text.literal("§aPosition of username set to: " + positionOfUsername), false);
                                                                    } else {
                                                                        client.player.sendMessage(Text.literal(alreadyChangedText + positionOfUsername), false);
                                                                    }
                                                                }
                                                                return 1;
                                                            })
                                                    )
                                            )
                                            // message component size
                                            .then(literal("messagecomponentsize")
                                                    .then(argument("size", IntegerArgumentType.integer())
                                                            .executes(context -> {
                                                                int newSize = IntegerArgumentType.getInteger(context, "size");
                                                                MinecraftClient client = MinecraftClient.getInstance();
                                                                if (client != null && client.player != null) {
                                                                    if (paymentMessageComponentsSize != newSize) {
                                                                        paymentMessageComponentsSize = newSize;
                                                                        saveConfigToJSON();
                                                                        client.player.sendMessage(Text.literal("§aSize of message component array set to: " + paymentMessageComponentsSize), false);
                                                                    } else {
                                                                        client.player.sendMessage(Text.literal(alreadyChangedText + paymentMessageComponentsSize), false);
                                                                    }
                                                                }
                                                                return 1;
                                                            })
                                                    )
                                            )
                                        )
                                )
            );
        });
    }
}


