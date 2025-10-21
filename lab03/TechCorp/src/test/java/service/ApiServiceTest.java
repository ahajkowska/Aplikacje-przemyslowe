package service;

import exception.ApiException;
import model.Employee;
import model.Position;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private ApiService apiService;

    private static final String API_URL = "https://jsonplaceholder.typicode.com/users";

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        apiService = new ApiService(API_URL, httpClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // POPRAWNA ODPOWIEDZ API

    @Nested
    @DisplayName("Successful API response scenarios")
    class SuccessfulResponseTests {

        @Test
        @DisplayName("should return list of employees when API response is valid")
        void shouldReturnEmployeeList_whenApiResponseIsValid() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals(1, employees.size());
        }

        @Test
        @DisplayName("should parse first name correctly from API response")
        void shouldParseFirstNameCorrectly_fromApiResponse() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals("Jan", employees.get(0).getFirstName());
        }

        @Test
        @DisplayName("should parse last name correctly from API response")
        void shouldParseLastNameCorrectly_fromApiResponse() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals("Kowalski", employees.get(0).getLastName());
        }

        @Test
        @DisplayName("should parse email correctly from API response")
        void shouldParseEmailCorrectly_fromApiResponse() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals("jan@example.com", employees.get(0).getEmail());
        }

        @Test
        @DisplayName("should parse company name correctly from API response")
        void shouldParseCompanyNameCorrectly_fromApiResponse() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals("TechCorp", employees.get(0).getCompany());
        }

        @Test
        @DisplayName("should assign DEVELOPER position to all employees from API")
        void shouldAssignDeveloperPosition_toAllEmployees() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals(Position.DEVELOPER, employees.get(0).getPosition());
        }

        @Test
        @DisplayName("should assign base salary to all employees from API")
        void shouldAssignBaseSalary_toAllEmployees() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals(Position.DEVELOPER.getBaseSalary(), employees.get(0).getSalary());
        }

        @Test
        @DisplayName("should parse multiple employees from API response")
        void shouldParseMultipleEmployees_fromApiResponse() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    },
                    {
                        "name": "Anna Nowak",
                        "email": "anna@example.com",
                        "company": {"name": "DataCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals(2, employees.size());
        }

        @Test
        @DisplayName("should parse first employee correctly when multiple employees")
        void shouldParseFirstEmployee_whenMultipleEmployees() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    },
                    {
                        "name": "Anna Nowak",
                        "email": "anna@example.com",
                        "company": {"name": "DataCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals("Jan", employees.get(0).getFirstName());
        }

        @Test
        @DisplayName("should parse second employee correctly when multiple employees")
        void shouldParseSecondEmployee_whenMultipleEmployees() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    },
                    {
                        "name": "Anna Nowak",
                        "email": "anna@example.com",
                        "company": {"name": "DataCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertEquals("Anna", employees.get(1).getFirstName());
        }

        @Test
        @DisplayName("should return empty list when API response is empty array")
        void shouldReturnEmptyList_whenApiResponseIsEmptyArray() throws Exception {
            String jsonResponse = "[]";

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            List<Employee> employees = apiService.fetchEmployeesFromApi();

            assertTrue(employees.isEmpty());
        }
    }

    // BŁĘDY HTTP

    @Nested
    @DisplayName("HTTP error handling - status codes")
    class HttpErrorTests {

        @Test
        @DisplayName("should throw ApiException when status code is 404")
        void shouldThrowApiException_whenStatusCodeIs404() throws Exception {
            when(httpResponse.statusCode()).thenReturn(404);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }

        @Test
        @DisplayName("should throw ApiException when status code is 500")
        void shouldThrowApiException_whenStatusCodeIs500() throws Exception {
            when(httpResponse.statusCode()).thenReturn(500);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }

        @Test
        @DisplayName("should not return employees when status code is not 200")
        void shouldNotReturnEmployees_whenStatusCodeIsNot200() throws Exception {
            when(httpResponse.statusCode()).thenReturn(404);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }
    }

    // BŁĘDY PARSOWANIA

    @Nested
    @DisplayName("JSON parsing error handling")
    class ParsingErrorTests {

        @Test
        @DisplayName("should throw ApiException when JSON is invalid")
        void shouldThrowApiException_whenJsonIsInvalid() throws Exception {
            String invalidJson = "{ invalid json }";

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(invalidJson);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }

        @Test
        @DisplayName("should throw ApiException when JSON is missing name field")
        void shouldThrowApiException_whenJsonIsMissingNameField() throws Exception {
            String jsonResponse = """
                [
                    {
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }

        @Test
        @DisplayName("should throw ApiException when JSON is missing email field")
        void shouldThrowApiException_whenJsonIsMissingEmailField() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }

        @Test
        @DisplayName("should throw ApiException when JSON is missing company field")
        void shouldThrowApiException_whenJsonIsMissingCompanyField() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com"
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }

        @Test
        @DisplayName("should throw ApiException when JSON is missing company name field")
        void shouldThrowApiException_whenJsonIsMissingCompanyNameField() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }

        @Test
        @DisplayName("should throw ApiException when response is not JSON array")
        void shouldThrowApiException_whenResponseIsNotJsonArray() throws Exception {
            String jsonResponse = """
                {
                    "name": "Jan Kowalski",
                    "email": "jan@example.com",
                    "company": {"name": "TechCorp"}
                }
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }

        @Test
        @DisplayName("should throw ApiException when response is empty string")
        void shouldThrowApiException_whenResponseIsEmptyString() throws Exception {
            String jsonResponse = "";

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }

        @Test
        @DisplayName("should throw ApiException when response is null")
        void shouldThrowApiException_whenResponseIsNull() throws Exception {
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(null);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            assertThrows(ApiException.class, () -> apiService.fetchEmployeesFromApi());
        }
    }

    // WERYFIKACJA WYWOŁAŃ (MOCKITO VERIFY)

    @Nested
    @DisplayName("Mockito verification tests - ensuring correct interactions")
    class VerificationTests {

        @Test
        @DisplayName("should call httpClient send exactly once when fetching employees")
        void shouldCallHttpClientSendOnce_whenFetchingEmployees() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            apiService.fetchEmployeesFromApi();

            verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        }

        @Test
        @DisplayName("should call statusCode on response when processing response")
        void shouldCallStatusCode_whenProcessingResponse() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            apiService.fetchEmployeesFromApi();

            verify(httpResponse, times(1)).statusCode();
        }

        @Test
        @DisplayName("should call body on response when status code is 200")
        void shouldCallBody_whenStatusCodeIs200() throws Exception {
            String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {"name": "TechCorp"}
                    }
                ]
                """;

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(jsonResponse);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            apiService.fetchEmployeesFromApi();

            verify(httpResponse, times(1)).body();
        }

        @Test
        @DisplayName("should not call body on response when status code is not 200")
        void shouldNotCallBody_whenStatusCodeIsNot200() throws Exception {
            when(httpResponse.statusCode()).thenReturn(404);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);

            try {
                apiService.fetchEmployeesFromApi();
            } catch (ApiException e) {
                // Expected exception
            }

            verify(httpResponse, never()).body();
        }
    }
}
