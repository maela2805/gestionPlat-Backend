package GestionPlat.example.demo.modules.auth.service;

import GestionPlat.example.demo.modules.auth.dto.AuthResponse;
import GestionPlat.example.demo.modules.auth.dto.LoginRequest;
import GestionPlat.example.demo.modules.auth.dto.RegisterRequest;
import GestionPlat.example.demo.modules.auth.model.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    User getProfile(String email);
}
