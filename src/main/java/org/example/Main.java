package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader()
            .setSkipHeaderRecord(true)
            .setAllowDuplicateHeaderNames(false)
            .build();

    static final private String PATH_TO_FILE = "/Users/amyhoyt/Desktop/4530/nsw_property_data.csv";
    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String DB_NAME = "realEstate";
    private static final String COLLECTION_NAME = "properties";
    private static final int BATCH_SIZE = 1000;

    public static void main(String[] args) {
        System.out.println("Hello and welcome!");

        final Path csvFilePath = Paths.get(PATH_TO_FILE);

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DB_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            System.out.println("Connected to MongoDB!");

            try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT)) {
                System.out.println("File opened");
                System.out.println("Headers: " + parser.getHeaderNames());

                List<Document> batch = new ArrayList<>();
                int count = 0;

                for (final CSVRecord record : parser) {
                    final Map<String, String> recordValues = record.toMap();
                    Document doc = new Document(recordValues);
                    batch.add(doc);
                    count++;

                    // Insert in batches of 1000 for performance
                    if (batch.size() >= BATCH_SIZE) {
                        collection.insertMany(batch);
                        batch.clear();
                        System.out.println("Inserted " + count + " records so far...");
                    }
                }

                // Insert any remaining records
                if (!batch.isEmpty()) {
                    collection.insertMany(batch);
                }

                System.out.println("Done! Total records inserted: " + count);

            } catch (IOException e) {
                System.out.println("File open failed: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("MongoDB connection failed: " + e.getMessage());
        }
    }
}