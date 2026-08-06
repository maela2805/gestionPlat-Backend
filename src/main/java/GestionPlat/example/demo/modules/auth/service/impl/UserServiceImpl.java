package GestionPlat.example.demo.modules.auth.service.impl;

import GestionPlat.example.demo.modules.auth.dto.UserCreateRequest;
import GestionPlat.example.demo.modules.auth.dto.UserDTO;
import GestionPlat.example.demo.modules.auth.dto.UserUpdateRequest;
import GestionPlat.example.demo.modules.auth.model.Role;
import GestionPlat.example.demo.modules.auth.model.User;
import GestionPlat.example.demo.modules.auth.repository.RoleRepository;
import GestionPlat.example.demo.modules.auth.repository.UserRepository;
import GestionPlat.example.demo.modules.auth.service.UserService;
import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé !");
        }

        Boutique boutique = null;
        if (request.getBoutiqueId() != null) {
            boutique = boutiqueRepository.findById(request.getBoutiqueId()).orElse(null);
        }

        // Vérification de sécurité pour les créateurs non-administrateurs
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String currentEmail = auth.getName();
            User currentUser = userRepository.findByEmail(currentEmail).orElse(null);
            if (currentUser != null && currentUser.getRole() != null) {
                String currentRoleName = currentUser.getRole().getName();
                boolean isAdmin = "ROLE_SUPER_ADMIN".equals(currentRoleName) || "ROLE_ADMIN".equals(currentRoleName);
                if (!isAdmin) {
                    // Les employés/gérants de boutique ne peuvent créer QUE des Vendeurs ou Caissiers
                    if (!"ROLE_VENDEUR".equals(request.getRoleName()) && !"ROLE_CAISSIER".equals(request.getRoleName()) && !"ROLE_EMPLOYEE".equals(request.getRoleName())) {
                        throw new IllegalArgumentException("En tant que responsable de boutique, vous ne pouvez créer que des comptes Vendeur ou Caissier.");
                    }
                    // Forcer l'association à la boutique du créateur
                    if (currentUser.getBoutique() != null) {
                        boutique = currentUser.getBoutique();
                    }
                }
            }
        }

        String targetRoleName = (request.getRoleName() != null && !request.getRoleName().isBlank())
                ? request.getRoleName() : "ROLE_EMPLOYEE";

        Role role = roleRepository.findByName(targetRoleName)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(targetRoleName)
                        .description(targetRoleName)
                        .build()));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role)
                .boutique(boutique)
                .active(true)
                .build();

        return mapToDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            String updateRoleName = request.getRoleName();
            Role role = roleRepository.findByName(updateRoleName)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(updateRoleName)
                            .description(updateRoleName)
                            .build()));
            user.setRole(role);
        }

        if (request.getBoutiqueId() != null) {
            if (request.getBoutiqueId() <= 0L) {
                user.setBoutique(null);
            } else {
                Boutique boutique = boutiqueRepository.findById(request.getBoutiqueId()).orElse(null);
                user.setBoutique(boutique);
            }
        }

        return mapToDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id: " + id));

        user.setActive(!user.isActive());
        return mapToDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .active(user.isActive())
                .roleName(user.getRole() != null ? user.getRole().getName() : "ROLE_EMPLOYEE")
                .roleDescription(user.getRole() != null ? user.getRole().getDescription() : "")
                .boutiqueId(user.getBoutique() != null ? user.getBoutique().getId() : null)
                .boutiqueName(user.getBoutique() != null ? user.getBoutique().getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
