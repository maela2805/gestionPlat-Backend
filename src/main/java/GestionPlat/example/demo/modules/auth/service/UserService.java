package GestionPlat.example.demo.modules.auth.service;

import GestionPlat.example.demo.modules.auth.dto.UserCreateRequest;
import GestionPlat.example.demo.modules.auth.dto.UserDTO;
import GestionPlat.example.demo.modules.auth.dto.UserUpdateRequest;
import GestionPlat.example.demo.modules.auth.model.Role;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    List<Role> getAllRoles();
    UserDTO getUserById(Long id);
    UserDTO createUser(UserCreateRequest request);
    UserDTO updateUser(Long id, UserUpdateRequest request);
    UserDTO toggleUserStatus(Long id);
    void deleteUser(Long id);
}
