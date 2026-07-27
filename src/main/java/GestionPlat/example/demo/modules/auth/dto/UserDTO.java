package GestionPlat.example.demo.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    private String roleName;
    private String roleDescription;
    private Long boutiqueId;
    private String boutiqueName;
    private LocalDateTime createdAt;
}
