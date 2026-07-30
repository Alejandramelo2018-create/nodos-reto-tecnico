package com.nodo.retotecnico.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nodo.retotecnico.model.Buy;
import com.nodo.retotecnico.model.Cart;
import com.nodo.retotecnico.model.Content;
import com.nodo.retotecnico.model.ExpansionPack;
import com.nodo.retotecnico.model.Platform;
import com.nodo.retotecnico.model.User;
import com.nodo.retotecnico.repository.BuysRepository;
import com.nodo.retotecnico.repository.CartRepository;
import com.nodo.retotecnico.repository.ContentsRepository;
import com.nodo.retotecnico.repository.ExpansionPacksRepository;
import com.nodo.retotecnico.repository.PlatformsRepository;
import com.nodo.retotecnico.repository.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BackendIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentsRepository contentsRepository;

    @Autowired
    private PlatformsRepository platformsRepository;

    @Autowired
    private ExpansionPacksRepository expansionPacksRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BuysRepository buysRepository;

    private static final AtomicInteger COUNTER = new AtomicInteger(1000);

    @Test
    void registerAndLoginFlowWorksAndReturnsJwt() throws Exception {
        String username = unique("user");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "%s",
                          "password": "StrongPass1!",
                          "firstName": "Ana",
                          "lastName": "Perez",
                          "country": "Argentina",
                          "email": "%s@example.com"
                        }
                        """.formatted(username, username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "%s",
                          "password": "StrongPass1!"
                        }
                        """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void registerValidationErrorsReturnBadRequestMap() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "a",
                          "password": "123",
                          "firstName": "",
                          "lastName": "",
                          "country": "",
                          "email": "invalid"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists())
                .andExpect(jsonPath("$.country").exists())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void oauth2AuthorizationEndpointsRedirect() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("accounts.google.com")));

        mockMvc.perform(get("/oauth2/authorization/meta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("facebook.com")));
    }

    @Test
    void oauth2SuccessReturnsJwtWithMockedPrincipal() throws Exception {
        mockMvc.perform(get("/auth/oauth2/success")
                .with(oauth2Login().attributes(attrs -> {
                    attrs.put("email", "oauthuser@example.com");
                    attrs.put("name", "OAuth User");
                    attrs.put("provider", "google");
                })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.provider").value("google"))
                .andExpect(jsonPath("$.email").value("oauthuser@example.com"))
                .andExpect(jsonPath("$.name").value("OAuth User"));
    }

    @Test
    void securityMatrixRequiresAuthForProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/nodos/cart"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/nodos/users"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/nodos/contents"))
                .andExpect(status().isOk());
    }

    @Test
    void jwtAllowsPlatformCrudFlow() throws Exception {
        String token = tokenForUser(unique("platformUser"));

        String createResponse = mockMvc.perform(post("/nodos/platform/add")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Steam",
                          "url": "https://store.steampowered.com"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Integer platformId = Integer.valueOf(createResponse);

        mockMvc.perform(get("/nodos/platform/" + platformId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Steam"));

        mockMvc.perform(put("/nodos/platform/" + platformId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Steam Updated",
                          "url": "https://example.com"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Steam Updated"));

        mockMvc.perform(delete("/nodos/platform/" + platformId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Platform deleted successfully"));
    }

    @Test
    void jwtAllowsExpansionCrudFlow() throws Exception {
        String token = tokenForUser(unique("expUser"));

        String createResponse = mockMvc.perform(post("/nodos/expansionpacks/create")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Pack One",
                          "description": "desc",
                          "platforms": "PC",
                          "price": 20.5,
                          "category": "RPG",
                          "publicationDate": "2026-01-01",
                          "language": "es"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Integer packId = Integer.valueOf(createResponse);

        mockMvc.perform(get("/nodos/expansionpacks/" + packId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pack One"));

        mockMvc.perform(put("/nodos/expansionpacks/" + packId)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Pack One Updated",
                          "description": "desc updated"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pack One Updated"));

        mockMvc.perform(delete("/nodos/expansionpacks/" + packId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Expansion Pack deleted successfully"));
    }

    @Test
    void contentsDeleteExistingWorks() throws Exception {
        String token = tokenForUser(unique("contentUser"));

        Content content = new Content();
        ReflectionTestUtils.setField(content, "section", "news");
        ReflectionTestUtils.setField(content, "title", "Title");
        ReflectionTestUtils.setField(content, "description", "Description");
        ReflectionTestUtils.setField(content, "image", "https://example.com/img.png");
        Content saved = contentsRepository.save(content);
        Integer savedId = (Integer) ReflectionTestUtils.getField(saved, "id");

        mockMvc.perform(delete("/nodos/contents/" + savedId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void usersDeleteExistingWorks() throws Exception {
        String token = tokenForUser(unique("adminLike"));
        User target = persistUser(unique("target"), unique("target") + "@example.com");
        Integer targetId = (Integer) ReflectionTestUtils.getField(target, "id");

        mockMvc.perform(delete("/nodos/users/" + targetId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("User deleted successfully")));
    }

    @Test
    void cartFlowWithJwtWorksAndRecalculatesTotal() throws Exception {
        String username = unique("cartUser");
        String token = tokenForUser(username);

        Platform platform = new Platform();
        ReflectionTestUtils.setField(platform, "name", "Xbox");
        ReflectionTestUtils.setField(platform, "url", "https://xbox.com");
        platform = platformsRepository.save(platform);
        Integer platformId = (Integer) ReflectionTestUtils.getField(platform, "id");

        ExpansionPack pack = new ExpansionPack();
        ReflectionTestUtils.setField(pack, "name", "Expansion Cart");
        ReflectionTestUtils.setField(pack, "description", "desc");
        ReflectionTestUtils.setField(pack, "platforms", "PC");
        ReflectionTestUtils.setField(pack, "price", 15.75);
        ReflectionTestUtils.setField(pack, "category", "Shooter");
        ReflectionTestUtils.setField(pack, "publicationDate", "2026-02-02");
        ReflectionTestUtils.setField(pack, "language", "es");
        pack = expansionPacksRepository.save(pack);
        Integer packId = (Integer) ReflectionTestUtils.getField(pack, "id");

        mockMvc.perform(get("/nodos/cart")
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0.0));

        mockMvc.perform(post("/nodos/cart/add")
                .param("expansionId", packId.toString())
                .param("platformId", platformId.toString())
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.total").value(15.75));

        mockMvc.perform(delete("/nodos/cart/remove")
                .param("expansionId", packId.toString())
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.total").value(0.0));

        mockMvc.perform(delete("/nodos/cart/clear")
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void buysEndpointsWorkWithJwt() throws Exception {
        String username = unique("buyJwtUser");
        String token = tokenForUser(username);
        User user = userRepository.findByUsername(username).orElseThrow();

        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "status", "activo");
        ReflectionTestUtils.setField(cart, "total", 10.0);
        ReflectionTestUtils.setField(cart, "user", user);
        cart = cartRepository.save(cart);

        Buy buy = new Buy();
        ReflectionTestUtils.setField(buy, "cart", cart);
        ReflectionTestUtils.setField(buy, "purchaseDate", new Date());
        ReflectionTestUtils.setField(buy, "paymentMethod", "CARD");
        ReflectionTestUtils.setField(buy, "status", "PAID");
        ReflectionTestUtils.setField(buy, "totalPrice", 10.0);
        buy = buysRepository.save(buy);
        Integer buyId = (Integer) ReflectionTestUtils.getField(buy, "id");

        mockMvc.perform(get("/nodos/buys")
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/nodos/buys/" + buyId)
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void buysFlowWorksWithOAuth2Principal() throws Exception {
        User user = persistUser(unique("buyOauthUser"), unique("buymail") + "@example.com");

        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "status", "activo");
        ReflectionTestUtils.setField(cart, "total", 55.0);
        ReflectionTestUtils.setField(cart, "user", user);
        cart = cartRepository.save(cart);

        Buy buy = new Buy();
        ReflectionTestUtils.setField(buy, "cart", cart);
        ReflectionTestUtils.setField(buy, "purchaseDate", new Date());
        ReflectionTestUtils.setField(buy, "paymentMethod", "CARD");
        ReflectionTestUtils.setField(buy, "status", "PAID");
        ReflectionTestUtils.setField(buy, "totalPrice", 55.0);
        buy = buysRepository.save(buy);
        Integer buyId = (Integer) ReflectionTestUtils.getField(buy, "id");

        String email = (String) ReflectionTestUtils.getField(user, "email");

        mockMvc.perform(get("/nodos/buys")
                .with(oauth2Login().attributes(attrs -> {
                    attrs.put("email", email);
                    attrs.put("name", "Buyer");
                })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(buyId));

        mockMvc.perform(get("/nodos/buys/" + buyId)
                .with(oauth2Login().attributes(attrs -> {
                    attrs.put("email", email);
                    attrs.put("name", "Buyer");
                })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(buyId));
    }

    private String tokenForUser(String username) throws Exception {
        if (userRepository.findByUsername(username).isEmpty()) {
            String role = "ROLE_USER";
            if (username.contains("admin") || username.contains("Admin") || username.contains("expUser") || username.contains("platformUser") || username.contains("contentUser")) {
                role = "ROLE_ADMIN";
            }
            persistUser(username, username + "@example.com", role);
        }

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "%s",
                          "password": "StrongPass1!"
                        }
                        """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(loginResponse);
        return json.get("token").asText();
    }

    private User persistUser(String username, String email) {
        return persistUser(username, email, "ROLE_USER");
    }

    private User persistUser(String username, String email, String role) {
        User user = new User();
        ReflectionTestUtils.setField(user, "username", username);
        ReflectionTestUtils.setField(user, "password", passwordEncoder.encode("StrongPass1!"));
        ReflectionTestUtils.setField(user, "name", "Integration User");
        ReflectionTestUtils.setField(user, "firstName", "Integration");
        ReflectionTestUtils.setField(user, "lastName", "User");
        ReflectionTestUtils.setField(user, "country", "Argentina");
        ReflectionTestUtils.setField(user, "role", role);
        ReflectionTestUtils.setField(user, "email", email);
        ReflectionTestUtils.setField(user, "registrationDate", new Date());
        return userRepository.save(user);
    }

    private String unique(String prefix) {
        return prefix + COUNTER.incrementAndGet();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
