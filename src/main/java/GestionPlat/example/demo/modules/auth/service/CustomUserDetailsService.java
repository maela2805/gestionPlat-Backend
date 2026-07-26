package GestionPlat.example.demo.modules.auth.service;

import GestionPlat.example.demo.modules.auth.model.User;
import GestionPlat.example.demo.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));

        Set<GrantedAuthority> authorities = new HashSet<>();
        
        if (user.getRole() != null) {
            String roleName = user.getRole().getName();
            if (roleName != null) {
                authorities.add(new SimpleGrantedAuthority(roleName));
                if (roleName.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority(roleName.substring(5)));
                } else {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                }
            }

            // Accorder automatiquement toutes les autorisations fondamentales au Super Admin et Admin
            if ("ROLE_SUPER_ADMIN".equalsIgnoreCase(roleName) || "SUPER_ADMIN".equalsIgnoreCase(roleName) ||
                "ROLE_ADMIN".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("SUPER_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ADMIN"));
                authorities.add(new SimpleGrantedAuthority("WRITE_STOCK"));
                authorities.add(new SimpleGrantedAuthority("READ_STOCK"));
                authorities.add(new SimpleGrantedAuthority("DELETE_STOCK"));
                authorities.add(new SimpleGrantedAuthority("MANAGE_USERS"));
                authorities.add(new SimpleGrantedAuthority("MANAGE_ORDERS"));
                authorities.add(new SimpleGrantedAuthority("MANAGE_CAISSE"));
            }
            
            // Ajouter les permissions associées au rôle
            if (user.getRole().getPermissions() != null) {
                user.getRole().getPermissions().forEach(permission -> 
                    authorities.add(new SimpleGrantedAuthority(permission.getName()))
                );
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                authorities
        );
    }
}
