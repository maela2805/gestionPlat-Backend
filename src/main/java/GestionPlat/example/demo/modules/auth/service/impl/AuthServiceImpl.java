package GestionPlat.example.demo.modules.auth.service.impl;

import GestionPlat.example.demo.modules.auth.dto.AuthResponse;
import GestionPlat.example.demo.modules.auth.dto.LoginRequest;
import GestionPlat.example.demo.modules.auth.dto.RegisterRequest;
import GestionPlat.example.demo.modules.auth.model.Role;
import GestionPlat.example.demo.modules.auth.model.User;
import GestionPlat.example.demo.modules.auth.repository.RoleRepository;
import GestionPlat.example.demo.modules.auth.repository.UserRepository;
import GestionPlat.example.demo.modules.auth.service.AuthService;
import GestionPlat.example.demo.modules.auth.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé !");
        }

        // L'inscription publique via le bouton "Créer un compte" crée exclusivement des comptes CLIENT.
        // Les comptes du personnel (EMPLOYEE, MANAGER, ADMIN) sont créés par l'Administrateur dans la gestion des utilisateurs.
        Role userRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_CLIENT")
                        .description("Client externe - Boutique et commandes en ligne")
                        .build()));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(userRole)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(userRole.getName())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Long boutiqueId = user.getBoutique() != null ? user.getBoutique().getId() : null;
        String boutiqueName = user.getBoutique() != null ? user.getBoutique().getName() : null;

        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() != null ? user.getRole().getName() : "ROLE_EMPLOYEE")
                .boutiqueId(boutiqueId)
                .boutiqueName(boutiqueName)
                .build();
    }

    @Override
    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}
