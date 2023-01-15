package taxi.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import taxi.exception.RegistrationException;
import taxi.lib.Injector;
import taxi.model.Driver;
import taxi.util.ConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DriverServiceImplTest {
    private static final String LOGIN = "alice";
    private static final String PASSWORD = "1234";
    private static final String NAME = "Alice";
    private static final String LICENSE_NUMBER = "FK2569";
    private static final Injector injector = Injector.getInstance("taxi");
    private static DriverDao driverDao;
    private static DriverService driverService;
    private Driver expectedDriver;

    @BeforeAll
    static void beforeAll() {
        driverDao = (DriverDao) injector.getInstance(DriverDao.class);
        driverService = (DriverService) injector.getInstance(DriverService.class);

    }

    @BeforeEach
    void setUp() {
        expectedDriver = new Driver(NAME, LICENSE_NUMBER, LOGIN, PASSWORD);
    }

    @Order(1)
    @Test
    void create_ok() {
        Driver driver = driverService.create(expectedDriver);
        assertDoesNotThrow(() -> driver,
                "Such login already exists. Please try another");
        assertTrue(findDriverByLogin(driver.getLogin()));
    }

    @Order(2)
    @Test
    void create_loginExist_notOk() {
        driverDao.create(expectedDriver);
        assertThrows(RegistrationException.class, () -> driverService.create(expectedDriver));
    }

    @Order(3)
    @Test
    void create_nullValues_notOk() {
        Driver nullDriver = new Driver(null, null, null, null);
        assertThrows(RuntimeException.class, () -> driverService.create(nullDriver));
    }

    @Order(4)
    @Test
    void create_blankLogin_notOk() {
        Driver driver = new Driver(NAME, LICENSE_NUMBER, "   ", PASSWORD);
        RegistrationException thrown = assertThrows(RegistrationException.class,
                () -> driverService.create(driver));
        assertEquals("Entry data can't be empty", thrown.getMessage());
    }

    @Order(5)
    @Test
    void findByLogin_ok() {
        driverDao.create(expectedDriver);
        assertDoesNotThrow(() -> driverService.findByLogin(expectedDriver.getLogin()),
                "Login or password was incorrect");

    }

    @Order(6)
    @Test
    void findByLogin_invalidLogin_notOk() {
        assertThrows(AuthenticationException.class, () -> driverService.findByLogin("Invalid"));
    }

    @Order(7)
    @Test
    void findByLogin_nullLogin_notOk() {
        assertThrows(AuthenticationException.class, () -> driverService.findByLogin(null));
    }

    @Order(8)
    @Test
    void get_ok() {
        Driver actualDriver = driverDao.create(expectedDriver);
        assertDoesNotThrow(() -> driverService.get(expectedDriver.getId()),
                "Can't get actualDriver by id: " + expectedDriver.getId());
        assertEquals(expectedDriver, actualDriver);
    }

    @Order(9)
    @Test
    void get_invalidId_notOk() {
        driverDao.create(expectedDriver);
        deleteDriver(expectedDriver);
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class,
                () -> driverService.get(expectedDriver.getId()));
        assertEquals("Can't get driver by id: " + expectedDriver.getId(), thrown.getMessage());
    }

    @Order(10)
    @Test
    void getAll_ok() {
        int expectedQuantityOfDrivers = getQuantityOfDrivers();
        int actualQuantityOfDrivers = driverService.getAll().size();
        assertEquals(expectedQuantityOfDrivers, actualQuantityOfDrivers);
    }

    @Order(11)
    @Test
    void update_ok() {
        driverDao.create(expectedDriver);
        expectedDriver.setLicenseNumber("AD1234");
        Driver actualDriver = driverService.update(expectedDriver);
        assertEquals(expectedDriver, actualDriver);
    }

    @Order(12)
    @Test
    void update_deletedDriver_notOk() {
        driverDao.create(expectedDriver);
        deleteDriver(expectedDriver);
        expectedDriver.setLicenseNumber("AD1234");
        Driver actualDriver = driverService.update(expectedDriver);
        assertEquals(expectedDriver, actualDriver);
    }

    @Order(13)
    @Test
    void delete_ok() {
        driverDao.create(expectedDriver);
        driverService.delete(expectedDriver.getId());
        assertFalse(findDriverByLogin(expectedDriver.getLogin()));
    }

    @AfterEach
    void tearDown() {
        deleteDriver(expectedDriver);
    }

    private boolean deleteDriver(Driver driver) {
        int result = 0;
        String deleteQuery = "DELETE FROM drivers WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
            if (Objects.nonNull(driver.getId())) {
                deleteStatement.setLong(1, driver.getId());
                result = deleteStatement.executeUpdate();
            }
            return result > 0;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't delete driver " + driver, e);
        }
    }

    private int getQuantityOfDrivers() {
        int driversQuantity = 0;
        String countDriversQuery = "SELECT COUNT(*) AS drivers_quantity "
                + "FROM drivers WHERE is_deleted = false;";
        try (Connection connection = ConnectionUtil.getConnection();
             PreparedStatement countDriversStatement = connection.prepareStatement(countDriversQuery)) {
            ResultSet resultSet = countDriversStatement.executeQuery();
            if (resultSet.next()) {
                driversQuantity = resultSet.getInt("drivers_quantity");
            }
            return driversQuantity;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get quantity of all drivers in DB", e);
        }
    }

    private boolean findDriverByLogin(String login) {
        String selectDriverQuery = "SELECT * FROM drivers WHERE login = ? AND is_deleted = false;";
        try (Connection connection = ConnectionUtil.getConnection();
        PreparedStatement findDriverByLoginStatement
                = connection.prepareStatement(selectDriverQuery)) {
            findDriverByLoginStatement.setString(1, login);
            ResultSet resultSet = findDriverByLoginStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException("Can't find driver by login " + login, e);
        }
    }
}
