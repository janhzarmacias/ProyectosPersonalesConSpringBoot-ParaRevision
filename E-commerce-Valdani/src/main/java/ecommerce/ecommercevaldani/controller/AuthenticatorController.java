package ecommerce.ecommercevaldani.controller;

import ecommerce.ecommercevaldani.config.JwtProvider;
import ecommerce.ecommercevaldani.model.Cart;
import ecommerce.ecommercevaldani.model.USER_ROLE;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.repository.CartRepository;
import ecommerce.ecommercevaldani.repository.UserRepository;
import ecommerce.ecommercevaldani.request.LoginRequest;
import ecommerce.ecommercevaldani.response.AuthResponse;
import ecommerce.ecommercevaldani.service.CustomerUserDetailService;
import ecommerce.ecommercevaldani.service.EmailServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/authenticator")
public class AuthenticatorController {


    @Autowired
    private UserRepository  userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private CustomerUserDetailService customerUserDetailService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private EmailServiceImp emailService;

    @PostMapping("/signup")
    public ResponseEntity<String> createUserHandler(@RequestBody User user) throws Exception {
        User existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser != null) {
            if (existingUser.isEnabled()) {
                // Ya verificado → no se permite registrar otra vez
                return new ResponseEntity<>("El correo ya está registrado y verificado.", HttpStatus.BAD_REQUEST);
            } else {
                // Ya existe pero no está verificado → reenviar verificación
                String newToken = UUID.randomUUID().toString();
                existingUser.setVerificationToken(newToken);
                existingUser.setTokenExpiration(LocalDateTime.now().plusHours(24));
                existingUser.setName(user.getName()); // Opcional: permitir actualizar nombre
                existingUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
                existingUser.setRole(user.getRole());

                userRepository.save(existingUser);

                emailService.sendVerificationEmail(existingUser.getEmail(), newToken);
                return new ResponseEntity<>("El usuario ya estaba registrado pero no verificado. Se envió un nuevo correo.", HttpStatus.OK);
            }
        }

        // Nuevo usuario
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setName(user.getName());
        newUser.setRole(user.getRole());
        newUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        newUser.setEnabled(false);

        String token = UUID.randomUUID().toString();
        newUser.setVerificationToken(token);
        newUser.setTokenExpiration(LocalDateTime.now().plusHours(24));

        userRepository.save(newUser);

        emailService.sendVerificationEmail(newUser.getEmail(), token);

        return new ResponseEntity<>("Se envió un correo de verificación. Revisa tu bandeja de entrada.", HttpStatus.CREATED);
    }



    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPasswordHash();

        try {
            Authentication authentication = authenticate(email, password);

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String rawRole = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();
            String cleanedRole = rawRole != null && rawRole.startsWith("ROLE_") ? rawRole.substring(5) : rawRole;

            String jwt = jwtProvider.generateToken(authentication);

            AuthResponse authResponse = new AuthResponse();
            authResponse.setJwt(jwt);
            authResponse.setMessage("Inicio de sesión exitoso");
            authResponse.setRole(USER_ROLE.valueOf(cleanedRole));

            return new ResponseEntity<>(authResponse, HttpStatus.OK);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "El correo no está registrado.")
            );
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Cuenta no verificada. Revisa tu correo.")
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Contraseña o correo incorrectos.")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Error interno. Intenta de nuevo.")
            );
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);

        if (userOptional.isEmpty()) {
            return new ResponseEntity<>("Token inválido", HttpStatus.BAD_REQUEST);
        }

        User user = userOptional.get();

        if (user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            return new ResponseEntity<>("El token ha expirado", HttpStatus.BAD_REQUEST);
        }

        if (user.isEnabled()) {
            return new ResponseEntity<>("Cuenta ya verificada", HttpStatus.OK);
        }

        user.setEnabled(true);
        userRepository.save(user);

        if (user.getCart() == null) {
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }

        return new ResponseEntity<>("Cuenta verificada con éxito. Ya puedes iniciar sesión.", HttpStatus.OK);
    }


    private Authentication authenticate(String email, String password) {

        UserDetails userDetails = customerUserDetailService.loadUserByUsername(email);
        if (userDetails == null) {
            throw new UsernameNotFoundException("Invalid email...");
        }

        // Verifica si está habilitado
        User user = userRepository.findByEmail(email);
        if (!user.isEnabled()) {
            throw new DisabledException("Cuenta no verificada. Revisa tu correo.");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password...");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}
