package com.roulettepaymenttracker.client;

import com.roulettepaymenttracker.RoulettePaymentDatabaseConnection;
import net.fabricmc.api.ClientModInitializer;

public class RoulettePaymentTrackerClient implements ClientModInitializer {

    RoulettePaymentCollector paymentCollector = new RoulettePaymentCollector();
    RoulettePaymentDatabaseConnection databaseConnection = new RoulettePaymentDatabaseConnection();

    @Override
    public void onInitializeClient() {
        paymentCollector.registerListener((paymentUser, paymentAmount) -> {
            databaseConnection.upsertPayment(paymentUser, paymentAmount); // sends query to database
        });
    }
}
