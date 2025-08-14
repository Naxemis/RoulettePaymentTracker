package com.roulettepaymenttracker.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

public class PaymentCollector {
    private static final ActionBarNotification actionBarNotification = new ActionBarNotification();
    private static final PlaySoundEffect playSoundEffect = new PlaySoundEffect();
    private static final PaymentCollectorCommands paymentCollectorCommands = new PaymentCollectorCommands();

    public String paymentUsername; // name of the user that sent the payment
    public int paymentAmount; // amount that user sent in payment

    public void registerListener(BiConsumer<String, Integer> onPaymentReceived) {
        ClientReceiveMessageEvents.GAME.register((text, overlay) -> {
            List<Text> paymentComponents = new ArrayList<>();
            collectAllTextComponents(text, paymentComponents); // collects all components

            try {
                String[] componentArray = paymentComponents.getFirst().getString().split(" ");

                // checks if the first word specified by player and first word, that have position specified by player, the same
                boolean areFirstWordsEqual = componentArray[paymentCollectorCommands.getPositionOfSpecifiedWord()].equals(paymentCollectorCommands.getSpecifiedComponentWord());
                boolean isSizeEqual = paymentComponents.size() == paymentCollectorCommands.getPaymentMessageComponentsSize();

                if(areFirstWordsEqual && isSizeEqual) {
                    try {
                        String priceContentComponent = componentArray[paymentCollectorCommands.getPositionOfAmount()]; // get the part of the message that starts with payment amount
                        this.paymentAmount = Integer.parseInt(StringUtils.getDigits(priceContentComponent));

                        this.paymentUsername = componentArray[paymentCollectorCommands.getPositionOfUsername()]; // gets payment username

                        onPaymentReceived.accept(paymentUsername, paymentAmount);  // notify the callback
                    } catch (Exception exception) {
                        System.out.println("Failed to retrieve payment price and username: " + exception.getMessage());
                        actionBarNotification.sendMessage("Failed to retrieve payment price and username", "§4");
                        playSoundEffect.playSound(SoundEvents.ENTITY_ITEM_BREAK);
                    }
                }
            } catch (Exception exception) {
                // Ignore
            }
        });
    }

    // changes specified component from payment message to string
    private String extractStringFromComponent(TextContent component) throws Exception {
        Method priceMethod = component.getClass().getDeclaredMethod("string"); // gets the method .string() from the calss that is not showed
        priceMethod.setAccessible(true); // bypasses Java access checks444

        return (String) priceMethod.invoke(component); // calls method on the object wiht no arguments
    }

    // collects all components from payment message and turns them into a List<Text>
    private void collectAllTextComponents(Text root, List<Text> collector) {
        for (Text sibling : root.getSiblings()) {
            collector.add(sibling);
            collectAllTextComponents(sibling, collector);
        }
    }
}