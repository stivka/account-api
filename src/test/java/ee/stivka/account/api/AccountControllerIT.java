package ee.stivka.account.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.stivka.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AccountControllerIT {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private AccountRepository repository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    repository.deleteAll();
  }

  @Test
  void createReadUpdateDelete_happyPath() throws Exception {
    String createBody = """
        {"name":"Alice","phoneNr":"+3725551234"}
        """;

    MvcResult created = mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", matchesPattern("/accounts/\\d+")))
        .andExpect(jsonPath("$.id").value(notNullValue()))
        .andExpect(jsonPath("$.name").value("Alice"))
        .andExpect(jsonPath("$.phoneNr").value("+3725551234"))
        .andExpect(jsonPath("$.createdDtime").value(notNullValue()))
        .andExpect(jsonPath("$.modifiedDtime").value(notNullValue()))
        .andReturn();

    JsonNode body = objectMapper.readTree(created.getResponse().getContentAsString());
    long id = body.get("id").asLong();
    String createdAt = body.get("createdDtime").asText();

    mockMvc.perform(get("/accounts/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value((int) id))
        .andExpect(jsonPath("$.name").value("Alice"));

    String updateBody = """
        {"name":"Alicia","phoneNr":"+3725559999"}
        """;

    mockMvc.perform(put("/accounts/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Alicia"))
        .andExpect(jsonPath("$.phoneNr").value("+3725559999"))
        .andExpect(jsonPath("$.createdDtime").value(createdAt));

    mockMvc.perform(delete("/accounts/" + id))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/accounts/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }

  @Test
  void create_blankName_returns400() throws Exception {
    String body = """
        {"name":"","phoneNr":"+3725551234"}
        """;
    mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.errors[0].field").value("name"));
  }

  @Test
  void create_invalidPhone_returns400() throws Exception {
    String body = """
        {"name":"Alice","phoneNr":"not-a-phone"}
        """;
    mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("phoneNr"));
  }

  @Test
  void create_nullPhone_isAllowed() throws Exception {
    String body = """
        {"name":"Bob"}
        """;
    mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.phoneNr").value(equalTo(null)));
  }

  @Test
  void create_malformedJson_returns400() throws Exception {
    mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{bad json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
  }

  @Test
  void get_invalidIdType_returns400() throws Exception {
    mockMvc.perform(get("/accounts/abc"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
  }

  @Test
  void get_unknownId_returns404() throws Exception {
    mockMvc.perform(get("/accounts/9999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }

  @Test
  void update_unknownId_returns404() throws Exception {
    String body = """
        {"name":"Ghost"}
        """;
    mockMvc.perform(put("/accounts/9999")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_unknownId_returns404() throws Exception {
    mockMvc.perform(delete("/accounts/9999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void create_duplicatePhone_returns409() throws Exception {
    String body = """
        {"name":"Alice","phoneNr":"+3725551234"}
        """;
    mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CONFLICT"));
  }

  @Test
  void openApiDocs_available() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$.paths./accounts.post").value(notNullValue()))
        .andExpect(jsonPath("$.paths./accounts/{id}.get").value(notNullValue()));
  }
}
