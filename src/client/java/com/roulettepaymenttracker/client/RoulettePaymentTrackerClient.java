package com.roulettepaymenttracker.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class RoulettePaymentTrackerClient implements ClientModInitializer {

    PaymentCollector paymentCollector = new PaymentCollector();
    PaymentDataManager paymentDataManager = new PaymentDataManager();

    @Override
    public void onInitializeClient() {
        paymentCollector.registerListener((paymentUsername, paymentAmount) -> {
            paymentDataManager.saveData(paymentUsername, paymentAmount).exceptionally(expection -> { // saves data to JSON file
                System.out.println("Something went wrong when trying to run saving data to JSON file async operation: " + expection.getMessage()); // shows error if async operation failed
                return null;
            });
        });

        onClientShutdown();
    }

    public void onClientShutdown() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            paymentDataManager.clearData();
            paymentDataManager.async_process_shutdown();
        });
    }
}
