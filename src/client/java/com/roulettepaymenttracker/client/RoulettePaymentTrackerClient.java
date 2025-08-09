package com.roulettepaymenttracker.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class RoulettePaymentTrackerClient implements ClientModInitializer {

    PaymentCollector paymentCollector = new PaymentCollector();
    DatabaseConnection databaseConnection = new DatabaseConnection();

    public int rouletteSession = 0;

    @Override
    public void onInitializeClient() {
        paymentCollector.registerListener((paymentUser, paymentAmount) -> {
            databaseConnection.upsertPayment(paymentUser, paymentAmount, this.rouletteSession).exceptionally(expection -> { // sends data to database
                System.out.println("Async database operation failed: " + expection.getMessage()); // shows error if async operation failed
                return null;
            });
        });

        onClientShutdown();
    }

    public void onClientShutdown() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            databaseConnection.async_process_shutdown(); // closes database connection thread
        });
    }
}
