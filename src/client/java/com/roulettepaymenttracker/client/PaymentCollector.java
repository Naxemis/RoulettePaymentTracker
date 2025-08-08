package com.roulettepaymenttracker.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

public class PaymentCollector {

    private static final String paymentFirstComponent = "Otrzymałeś:"; // the word that will be checked with payment message
    private static final int positionFirstWord = 1; // where's located first word that player want to use for checking
    private static final int positionAmount = 2; // where's located amount that player has sent
    private static final int positionUsername = 4; // where's located player's username
    private static final int paymentComponentsSize = 5; // what's the size of the payment message
    public String paymentUser; // name of the user that sent the payment
    public int paymentAmount; // amount that user sent in payment
    public void registerListener(BiConsumer<String, Integer> onPaymentReceived) {
        ClientReceiveMessageEvents.GAME.register((text, overlay) -> {
            List<Text> paymentComponents = new ArrayList<>();
            collectAllTextComponents(text, paymentComponents); // collects all components

            try {
                    TextContent firstContentComponent = paymentComponents.get(positionFirstWord).getContent(); // get the part of the message that starts with specified later word
                    String firstContentString = extractStringFromComponent(firstContentComponent);
                    String firstContentStringSpaceless = StringUtils.deleteWhitespace(firstContentString);

                    // checks if the first word specified by player and first word, that have position specified by player, the same
                    boolean areFirstWordsEqual = firstContentStringSpaceless.equals(paymentFirstComponent);

                    if(areFirstWordsEqual && paymentComponents.size() == paymentComponentsSize) {
                        try {
                            TextContent priceContentComponent = paymentComponents.get(positionAmount).getContent(); // get the part of the message that starts with payment amount
                            this.paymentAmount = Integer.parseInt(StringUtils.getDigits(extractStringFromComponent(priceContentComponent)));

                            this.paymentUser = paymentComponents.get(positionUsername).getString(); // gets payment username

                            onPaymentReceived.accept(paymentUser, paymentAmount);  // notify the callback
                        } catch (Exception exception) {
                            System.out.println("Failed to retrieve payment price and username: " + exception.getMessage());
                        }
                    }
            } catch (Exception exception) {
                System.out.println("Payment message pre-check failed: " + exception.getMessage());
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