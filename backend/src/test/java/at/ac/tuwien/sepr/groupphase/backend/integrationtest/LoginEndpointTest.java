package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.BaseTest;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.repository.BanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.DEFAULT_USER_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.ADMIN_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.LOGIN_BASE_URI;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.ArrayList;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "generateData"})
@AutoConfigureMockMvc
public class LoginEndpointTest extends BaseTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    public void validUserLogin() throws Exception {
        String loginData = "{\"password\": \"Password123\", \"email\": \"" + DEFAULT_USER_EMAIL + "\"}";
        ArrayList<String> expectedRole = new ArrayList<>();
        expectedRole.add("ROLE_USER");
        validLoginTest(loginData, expectedRole, DEFAULT_USER_EMAIL);
    }

    @Test
    public void validAdminLogin() throws Exception {
        String loginData = "{\"password\": \"Password123\", \"email\": \"" + ADMIN_EMAIL + "\"}";
        ArrayList<String> expectedRole = new ArrayList<>();
        expectedRole.add("ROLE_ADMIN");
        validLoginTest(loginData, expectedRole, ADMIN_EMAIL);
    }

    @Test
    public void UserLoginWithNonexistentEmailReturnsNotFound() throws Exception {
        String loginData = "{\"password\": \"Password123\", \"email\": \"nonexistentEmail@student.tuwien.ac.at\"}";
        invalidLoginTest(loginData, HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void AdminLoginWithIncorrectPasswordNotFound() throws Exception {
        String loginData = "{\"password\": \"wrongPassword\", \"email\": \"" + ADMIN_EMAIL + "\"}";
        invalidLoginTest(loginData, HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void BanUserLoginShouldFail() throws Exception {
        var email = userRepository.findAll().stream().filter(item -> item.isBanned())
            .findFirst().get().getDetails().getEmail();
        String loginData = "{\"password\": \"Password123\", \"email\": \"" + email + "\"}";
        invalidLoginTest(loginData, HttpStatus.UNAUTHORIZED.value());
    }

    private void validLoginTest(String loginData, ArrayList<String> expectedRole, String expectedEmail) throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(LOGIN_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginData))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String responseBody = response.getContentAsString();
        String token = responseBody.replace("Bearer ", "");
        byte[] signingKey = securityProperties.getJwtSecret().getBytes();
        Claims claims = Jwts.parser()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();

        ArrayList<String> actualRole = (ArrayList<String>) claims.get("rol");
        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals("text/plain;charset=UTF-8", response.getContentType()),
            () -> assertEquals(expectedEmail, claims.get("sub")),
            () -> assertEquals(expectedRole, actualRole)
        );
    }

    private void invalidLoginTest(String loginData, int expectedStatus) throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post(LOGIN_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginData))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertAll(
            () -> assertEquals(expectedStatus, response.getStatus()),
            () -> assertEquals("text/plain;charset=UTF-8", response.getContentType())
        );
    }
}
