package com.estatepro;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.estatepro.entity.ListingStatus;
import com.estatepro.entity.Property;
import com.estatepro.entity.Role;
import com.estatepro.entity.User;
import com.estatepro.repository.PropertyRepository;
import com.estatepro.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:estatepro-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "estatepro.demo.password="
})
class MarketplaceIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PropertyRepository properties;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;

    @Test
    void publicSearchReturnsSeededListingsAndHonorsServerSideFilters() throws Exception {
        mvc.perform(get("/api/properties").param("city", "Chennai").param("size", "48"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].city").value("Chennai"));
    }

    @Test
    void registrationIssuesJwtAndBuyerCannotAccessAdminRoutes() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Test Buyer","email":"integration-buyer@example.test","password":"SecureDemoPass!2026","role":"BUYER"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("BUYER"))
                .andReturn();
        JsonNode response = mapper.readTree(result.getResponse().getContentAsString());
        String token = response.get("token").asText();

        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration-buyer@example.test"));
        mvc.perform(get("/api/recommendations").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void selfRegistrationCannotAssignPrivilegedRolesAndProtectedMutationsRequireAuthentication() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Pretend Admin","email":"pretend@example.test","password":"SecureDemoPass!2026","role":"ADMIN"}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/properties").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicContactFormStoresValidatedMessages() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Portfolio Visitor","email":"visitor@example.test","subject":"Finding a home","message":"Please help me explore Chennai."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Thanks for reaching out. The EstatePro team will be in touch."));
        mvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void sellerListingsStayPrivateUntilModeratedAndCannotBeEditedByAnotherSeller() throws Exception {
        String ownerToken = register("listing-owner@example.test", "SELLER");
        String otherToken = register("other-owner@example.test", "SELLER");
        MvcResult created = mvc.perform(post("/api/properties").header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"A private test home","description":"A home awaiting review.","propertyType":"Villa","listingType":"For Sale","city":"Cochin","locality":"Fort Kochi","address":"Fort Kochi","price":15000000,"bedrooms":3,"bathrooms":2,"areaSqft":1800,"imageUrl":"/assets/images/properties/villa-01.jpg"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        String id = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(get("/api/properties/" + id))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/properties/" + id).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/properties/" + id).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(delete("/api/properties/" + id).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void buyerAndAssignedAgentCanCompleteFavouriteLeadVisitBookingAndDemoPaymentWorkflow() throws Exception {
        String buyerToken = register("workflow-buyer@example.test", "BUYER");
        String otherBuyerToken = register("other-buyer@example.test", "BUYER");
        User agent = users.save(new User("workflow-agent@example.test", passwords.encode("AgentWorkflowPass!2026"),
                "Workflow Agent", Role.AGENT));
        Property property = properties.findByStatusOrderByCreatedAtDesc(ListingStatus.PUBLISHED).get(0);
        property.setAgent(agent);
        properties.save(property);
        String agentToken = login("workflow-agent@example.test", "AgentWorkflowPass!2026");

        mvc.perform(post("/api/favorites/" + property.getId()).header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/favorites/" + property.getId()).header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/favorites").header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(post("/api/enquiries").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":\"" + property.getId() + "\",\"message\":\"I would like to know more about this home.\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("NEW"));
        mvc.perform(get("/api/enquiries").header("Authorization", "Bearer " + agentToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        String date = java.time.LocalDate.now().plusDays(3).toString();
        MvcResult visit = mvc.perform(post("/api/appointments").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":\"" + property.getId() + "\",\"date\":\"" + date + "\",\"time\":\"10:00\"}"))
                .andExpect(status().isCreated()).andReturn();
        String appointmentId = mapper.readTree(visit.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(post("/api/appointments").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"propertyId\":\"" + property.getId() + "\",\"date\":\"" + date + "\",\"time\":\"10:00\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/appointments/" + appointmentId).header("Authorization", "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));

        MvcResult booking = mvc.perform(post("/api/bookings/" + property.getId()).header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REQUESTED")).andReturn();
        String bookingId = mapper.readTree(booking.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(patch("/api/bookings/" + bookingId).header("Authorization", "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/bookings/" + bookingId).header("Authorization", "Bearer " + otherBuyerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/payments/" + bookingId + "/demo").header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DEMO_SUCCESS"))
                .andExpect(jsonPath("$.transactionReference").value(org.hamcrest.Matchers.startsWith("DEMO-")));
    }

    private String register(String email, String role) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Workflow User\",\"email\":\"" + email
                                + "\",\"password\":\"WorkflowSecurePass!2026\",\"role\":\"" + role + "\"}"))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
