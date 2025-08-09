package com.roulettepaymenttracker.client;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.lang.reflect.Type;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentDataManager {

    private static final Gson gson = new Gson(); // creates Gson instance used for JSON serialization and deserialization
    private static final String filePath = System.getenv("APPDATA") + "/RoulettePaymentTracker/paymentData.json"; //file path to JSON file
    private static final Path paymentDataPath = Paths.get(filePath); // converts filePath string to a Path object
    private final ExecutorService executorService = Executors.newFixedThreadPool(2); // thread pool for database operations

    public CompletableFuture<Void> saveData(String paymentUsername, int paymentAmount) {

        return CompletableFuture.runAsync(() -> { // runs the operation on background thread
            PlayerDataHolder newPlayerData = new PlayerDataHolder(paymentUsername, paymentAmount); // hold new player's data
            List<PlayerDataHolder> listOfPlayerData = new ArrayList<>(); // holds all player's data

            if (Files.exists(paymentDataPath)) { // check if .json file already exists
                try (Reader fileReader = Files.newBufferedReader(paymentDataPath)) { // reads existing data
                    Type listType = new TypeToken<List<PlayerDataHolder>>(){}.getType(); // defines expected type od List<PlayerDataHolder>
                    listOfPlayerData = gson.fromJson(fileReader, listType); // deserialize JSON array into list of PlayerDataHolder objects


                    // if file was null, initialize an empty list to avoid NullPointerException
                    if (listOfPlayerData == null) {
                        listOfPlayerData = new ArrayList<>();
                    }

                }
                catch (IOException exception) {
                    System.out.println("Something went wrong reading existing data: " + exception.getMessage());
                }
            }

            boolean playerAlreadyExists = false;
            for (int index = 0; index < listOfPlayerData.size(); index++) {
                PlayerDataHolder player = listOfPlayerData.get(index);

                if (player.username().equals(paymentUsername)) {
                    int updatedAmount = player.amount() + paymentAmount; // updates payment amount
                    PlayerDataHolder updatedPlayer = new PlayerDataHolder(paymentUsername, updatedAmount); // creates object with updates payment data

                    listOfPlayerData.set(index, updatedPlayer); // puts updated player data in the place of the old data

                    playerAlreadyExists = true;
                    break; // exits loop
                }
            }

            if (!playerAlreadyExists) {
                listOfPlayerData.add(newPlayerData); // adds new player data to list of player data
            }

            // write the updated list back to the JSON file
            try (FileWriter fileWriter = new FileWriter(filePath)) {
                gson.toJson(listOfPlayerData, fileWriter);
                System.out.println("Succesfully saved data to JSON file");
            } catch (IOException exception) {
                System.out.println("Something went wrong during saving data to JSON file: " + exception.getMessage());
            }
        }, executorService); // makes the method use dedicated thread pool for execution
    }

    public void async_process_shutdown() {
        executorService.shutdown(); // closes the database connection thread
        System.out.println("Closing Payment Data Manager thread.");
    }

    public void clearData() {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write("[]");
            System.out.println("JSON file has been cleared properly.");
        }
        catch (IOException exception) {
            System.out.println("Something went wrong when trying to clear JSON file: " + exception);
        }
    }
}
