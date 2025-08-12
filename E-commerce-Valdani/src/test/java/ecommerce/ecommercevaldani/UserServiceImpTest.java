package ecommerce.ecommercevaldani;

import ecommerce.ecommercevaldani.config.JwtProvider;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.repository.UserRepository;
import ecommerce.ecommercevaldani.service.UserServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImpTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private UserServiceImp userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findUserByjwtToken_shouldReturnUser() throws Exception {
        String token = "mock-jwt-token";
        String email = "test@example.com";

        User user = new User();
        user.setEmail(email);
        user.setAddresses(Collections.emptyList()); // evita nullPointer con getAddresses().size()

        when(jwtProvider.getEmailFromJwtToken(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(user);

        User result = userService.findUserByjwtToken(token);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void findUserByEmail_shouldReturnUser_whenUserExists() throws Exception {
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(user);

        User result = userService.findUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findUserByEmail_shouldThrowException_whenUserNotFound() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> {
            userService.findUserByEmail(email);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void ping_shouldReturnTrueAndCallCount() {
        when(userRepository.count()).thenReturn(1L);

        boolean result = userService.ping();

        assertTrue(result);
        verify(userRepository).count();
    }
}