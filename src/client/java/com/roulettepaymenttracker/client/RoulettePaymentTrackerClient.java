package com.roulettepaymenttracker.client;

import net.fabricmc.api.ClientModInitializer;

public class RoulettePaymentTrackerClient implements ClientModInitializer {

    RoulettePaymentCollector collector = new RoulettePaymentCollector();

    @Override
    public void onInitializeClient() {
        collector.getMessage();
    }
}
