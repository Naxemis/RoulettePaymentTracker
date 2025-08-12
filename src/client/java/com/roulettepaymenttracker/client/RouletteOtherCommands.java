package com.roulettepaymenttracker.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class RouletteOtherCommands {
    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("roulette")
                            .then(ClientCommandManager.literal("help")
                                    .executes(context -> {
                                        MinecraftClient client = MinecraftClient.getInstance();
                                        if (client != null && client.player != null) {
                                            client.player.sendMessage(Text.literal("§6[---- Roulette Help ----]"), false);

                                            client.player.sendMessage(Text.literal(""),false);

                                            client.player.sendMessage(Text.literal("§a/roulette collectorconfig info"), false);
                                            client.player.sendMessage(Text.literal("§7Shows the current payment collector configuration."),false);

                                            client.player.sendMessage(Text.literal(""),false);

                                            client.player.sendMessage(Text.literal("§a/roulette collectorconfig set specifiedcomponentword <word>"), false);
                                            client.player.sendMessage(Text.literal("§7Sets the keyword that will be detected in payment messages."), false);

                                            client.player.sendMessage(Text.literal(""),false);

                                            client.player.sendMessage(Text.literal("§a/roulette collectorconfig set positionofspecifiedword <number>"), false);
                                            client.player.sendMessage(Text.literal("§7Sets the position of the keyword in the message, used to identify payment messages."), false);

                                            client.player.sendMessage(Text.literal(""),false);

                                            client.player.sendMessage(Text.literal("§a/roulette collectorconfig set positionofamount <number>"), false);
                                            client.player.sendMessage(Text.literal("§7Sets the position of the payment amount in the message."), false);

                                            client.player.sendMessage(Text.literal(""),false);

                                            client.player.sendMessage(Text.literal("§a/roulette collectorconfig set positionofusername <number>"), false);
                                            client.player.sendMessage(Text.literal("§7Sets the position of the username in the message."), false);

                                            client.player.sendMessage(Text.literal(""),false);

                                            client.player.sendMessage(Text.literal("§a/roulette collectorconfig set messagecomponentsize <number>"), false);
                                            client.player.sendMessage(Text.literal("§7Sets the expected number of components in the payment message."), false);
                                        }
                                        return 1;
                                    })
                            )
            );
        });
    }
}
