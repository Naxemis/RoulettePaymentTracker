package com.roulettepaymenttracker.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

public class RoulettePaymentTrackerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        getMessage();
    }

    static String paymentFirstComponent = "Otrzymałeś:";
    String paymentAmount;
    String paymentUser;
    public void getMessage() {
        ClientReceiveMessageEvents.GAME.register((text, overlay) -> {
            List<Text> paymentComponents = new ArrayList<>();
            collectAllTextComponents(text, paymentComponents); // collects all components

            try {
                    TextContent firstContentComponent = paymentComponents.get(1).getContent(); // get the part of the message that starts with payment amount
                    Method firstContentMethod = firstContentComponent.getClass().getDeclaredMethod("string"); // gets the method .string() from the calss that is not showed
                    firstContentMethod.setAccessible(true); // bypasses Java access checks

                    String firstContentString = (String) firstContentMethod.invoke(firstContentComponent); // calls method on the object wiht no arguments

                    if(StringUtils.deleteWhitespace(firstContentString).equals(StringUtils.deleteWhitespace(paymentFirstComponent)) && paymentComponents.size() == 5) {
                        try {
                            TextContent priceContentComponent = paymentComponents.get(2).getContent(); // get the part of the message that starts with payment amount
                            Method priceMethod = priceContentComponent.getClass().getDeclaredMethod("string"); // gets the method .string() from the calss that is not showed
                            priceMethod.setAccessible(true); // bypasses Java access checks

                            this.paymentAmount = (String) priceMethod.invoke(priceContentComponent); // calls method on the object wiht no arguments
                            System.out.println("Payment Amount: " + this.paymentAmount);

                            this.paymentUser = paymentComponents.get(4).getString(); // gets payment username
                            System.out.println("Payment User: " + this.paymentUser);
                        } catch (Exception exception) {
                            System.out.println("Failed to retrieve payment price and username: " + exception.getMessage());
                        }
                    }
            } catch (Exception exception) {
                System.out.println("Payment message pre-check failed: " + exception.getMessage());
            }
        });
    }

    private void collectAllTextComponents(Text root, List<Text> collector) {
        for (Text sibling : root.getSiblings()) {
            collector.add(sibling);
            collectAllTextComponents(sibling, collector);
        }
    }
}