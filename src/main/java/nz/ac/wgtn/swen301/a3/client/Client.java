package nz.ac.wgtn.swen301.a3.client;

import java.io.IOException;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * The Client class fetches log statistics in CSV or Excel format from the server
 * and stores them in a local file.
 *
 * Usage:
 *   java -cp <classpath> nz.ac.wgtn.swen301.a3.client.Client <type> <fileName>
 *
 *   - type: "csv" or "excel" indicating the format of data to fetch
 *   - fileName: Local file path to store the fetched data
 *
 * Example:
 *   java -cp target/classes nz.ac.wgtn.swen301.a3.client.Client excel logs.xlsx
 */
public class Client {
    public static void main(String[] args) {
        // Check tjat exactly two arguments are provided
        if (args.length != 2) {
            System.err.println("Usage: java -cp <classpath> nz.ac.wgtn.swen301.a3.client.Client <type> <fileName>");
            System.err.println("  <type>: 'csv' or 'excel'");
            System.err.println("  <fileName>: Local file path to store the fetched data");
            System.exit(1);
        }

        String type = args[0].toLowerCase();
        String fileName = args[1];

        // Make sure there is the 'type' argument
        if (!type.equals("csv") && !type.equals("excel")) {
            System.err.println("Error: <type> must be either 'csv' or 'excel'");
            System.exit(1);
        }

        // Construct the URL based on the type
        String url = "http://localhost:8080/logstore/stats/" + type;

        // Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        // Build the HTTP GET request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            // Send the request and get the response as byte array
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            // Check that the response status code is 200
            if (response.statusCode() == 200) {
                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                    fos.write(response.body());
                    System.out.println("Data successfully downloaded to " + fileName);
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                    System.exit(1);
                }
            } else {
                System.err.println("Error: Server responded with status code " + response.statusCode());
                System.exit(1);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: Unable to connect to the server at " + url);
            System.err.println("Details: " + e.getMessage());
            System.exit(1);
        }
    }
}
