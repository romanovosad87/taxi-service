package taxi.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import taxi.dao.DriverDao;
import taxi.exception.AuthenticationException;
import taxi.exception.DataProcessingException;
import taxi.lib.Injector;
import taxi.model.Driver;
import taxi.util.ConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthenticationServiceImplTest {
    private static final String LOGIN = "alice";
    private static final String PASSWORD = "1234";
    private static final String NAME = "Alice";
    private static final String LICENSE_NUMBER = "FK2569";
    private static final Injector injector = Injector.getInstance("taxi");
    private static AuthenticationService authenticationService;
    private static DriverDao driverDao;
    private Driver expectedDriver;

    @BeforeAll
    static void beforeAll() {
        driverDao = (DriverDao) injector.getInstance(DriverDao.class);
        authenticationService = (AuthenticationService) injector.getInstance(AuthenticationService.class);
    }

    @BeforeEach
    void setUp() {
        expectedDriver = driverDao.create(new Driver(NAME, LICENSE_NUMBER, LOGIN, PASSWORD));
    }

    @Test
    @Order(1)
    void login_ok() throws AuthenticationException {
        assertDoesNotThrow(() -> authenticationService.login(LOGIN, PASSWORD),
                "Login or password was incorrect");
        Driver actualDriver = authenticationService.login(LOGIN, PASSWORD);
        assertEquals(expectedDriver, actualDriver);
    }

    @Test
    @Order(2)
    void login_loginNull_notOk() {
        assertThrows(AuthenticationException.class, () ->
                authenticationService.login(null, PASSWORD));
    }

    @Test
    @Order(3)
    void login_emptyLineForLogin_notOk() {
        assertThrows(AuthenticationException.class, () ->
                authenticationService.login("", PASSWORD));
    }

    @Test
    @Order(4)
    void login_invalidLogin_notOk() {
        assertThrows(AuthenticationException.class, () ->
                authenticationService.login("Invalid", PASSWORD));
    }

    @Order(5)
    @Test
    void login_nullPassword_notOk() {
        assertThrows(AuthenticationException.class, () ->
                authenticationService.login(LOGIN, null));
    }

    @AfterEach
    void tearDown() {
        deleteDriver(expectedDriver);
    }

    private boolean deleteDriver(Driver driver) {
        String deleteQuery = "DELETE FROM drivers WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
            deleteStatement.setLong(1, driver.getId());
            return deleteStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't delete driver " + driver, e);
        }
    }
}
