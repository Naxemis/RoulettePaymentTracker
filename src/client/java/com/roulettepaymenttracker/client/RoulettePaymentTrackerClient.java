package com.roulettepaymenttracker.client;

import com.roulettepaymenttracker.DatabaseConnection;
import net.fabricmc.api.ClientModInitializer;

public class RoulettePaymentTrackerClient implements ClientModInitializer {

    PaymentCollector paymentCollector = new PaymentCollector();
    DatabaseConnection databaseConnection = new DatabaseConnection();

    @Override
    public void onInitializeClient() {
        paymentCollector.registerListener((paymentUser, paymentAmount) -> {
            databaseConnection.upsertPayment(paymentUser, paymentAmount); // sends query to database
        });
    }
}
