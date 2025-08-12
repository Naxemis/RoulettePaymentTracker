package com.roulettepaymenttracker.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class RoulettePaymentTrackerClient implements ClientModInitializer {

    RouletteCommands rouletteCommands = new RouletteCommands();
    PaymentCollector paymentCollector = new PaymentCollector();
    PaymentDataManager paymentDataManager = new PaymentDataManager();

    private int tickCounter = 0;
    private static final int updateWinnerDataTicks = 10; // 20 ticks == 1 second
    WinnerDataManager winnerDataManager = new WinnerDataManager();
    private void startWatchingWinnerData() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter >= updateWinnerDataTicks) {
                tickCounter = 0;
                winnerDataManager.updateWinnerData().exceptionally(expection -> {
                    System.out.println("Something went wrong when trying to run reading winner data from JSON file async operation: " + expection.getMessage());
                    return null;
                });
            }
        });
    }

    @Override
    public void onInitializeClient() {
        rouletteCommands.register();

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            rouletteCommands.reset_roulette_status();
        });

        paymentCollector.registerListener((paymentUsername, paymentAmount) -> {
            paymentDataManager.saveData(paymentUsername, paymentAmount).exceptionally(expection -> { // saves data to JSON file
                System.out.println("Something went wrong when trying to run saving data to JSON file async operation: " + expection.getMessage()); // shows error if async operation failed
                return null;
            });
        });

        startWatchingWinnerData();

        onClientShutdown();
    }

    public void onClientShutdown() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            paymentDataManager.clearData();
            paymentDataManager.async_process_shutdown();
            winnerDataManager.async_process_shutdown();
            rouletteCommands.reset_roulette_status();
        });
    }
}
