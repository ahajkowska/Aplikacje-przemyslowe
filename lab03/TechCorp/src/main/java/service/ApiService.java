package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import exception.ApiException;
import model.Employee;
import model.Position;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

public class ApiService {
    private final String apiUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public ApiService(String apiUrl) {
        this.apiUrl = apiUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public List<Employee> fetchEmployeesFromApi() throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()); //status, body, headers

            if (response.statusCode() != 200) {
                throw new ApiException("API request failed with status code: " + response.statusCode());
            }

            return parseApiResponse(response.body());
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Error communicating with API", e);
        }
    }

    private List<Employee> parseApiResponse(String jsonResponse) throws ApiException {
        try {
            List<Employee> employees = new ArrayList<>();
            JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);

            for (JsonElement element : jsonArray) {
                JsonObject userObject = element.getAsJsonObject();

                // Bierzemy name i dzielimy na firstName i lastName
                String fullName = userObject.get("name").getAsString();
                String[] nameParts = fullName.split(" ", 2);
                String firstName = nameParts[0];
                String lastName = nameParts.length > 1 ? nameParts[1] : "";

                // Bierzemy email
                String email = userObject.get("email").getAsString();

                // Bierzemy company name
                String company = userObject.getAsJsonObject("company").get("name").getAsString();

                // Przypisanie wszystkim użytkownikom z API stanowiska programista + bazową stawkę
                Position position = Position.DEVELOPER;
                double salary = position.getBaseSalary();

                employees.add(new Employee(firstName, lastName, email, company, position, salary));
            }

            return employees;
        } catch (Exception e) {
            throw new ApiException("Error parsing API response", e);
        }
    }
}
