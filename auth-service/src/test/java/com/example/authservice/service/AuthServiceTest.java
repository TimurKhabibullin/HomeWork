package com.example.authservice.service;

import com.example.authservice.constants.Role;
import com.example.authservice.dto.LoginDto;
import com.example.authservice.dto.RegisterDto;
import com.example.authservice.dto.TokenDto;
import com.example.authservice.entity.Credential;
import com.example.authservice.entity.User;
import com.example.authservice.exception.ConflictException;
import com.example.authservice.exception.NotFoundException;
import com.example.authservice.repository.CredentialRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.AuthUser;
import com.example.authservice.transformer.CredentialTransformerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Timur Khabibullin
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String RAW_PASSWORD = "correct-horse-battery";

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @Mock
    private UserRepository userRepo;

    @Mock
    private CredentialRepository credentialRepo;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepo, credentialRepo, passwordEncoder, authenticationManager, tokenService,
                new CredentialTransformerImpl());
    }

    @Test
    void shouldRegisterWithNormalizedLoginAndEmailAndHashedPassword() {
        var dto = registerDto();
        when(credentialRepo.existsByLogin("alice_1")).thenReturn(false);
        when(userRepo.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(userRepo.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.<User>getArgument(0).setId(7L));
        when(credentialRepo.saveAndFlush(any(Credential.class)))
                .thenAnswer(inv -> inv.<Credential>getArgument(0).setId(7L));

        var profile = service.register(dto);

        assertThat(profile.getId()).isEqualTo(7L);
        assertThat(profile.getLogin()).isEqualTo("alice_1");
        assertThat(profile.getName()).isEqualTo("Alice");
        assertThat(profile.getEmail()).isEqualTo("alice@example.com");

        var captor = ArgumentCaptor.forClass(Credential.class);
        verify(credentialRepo).saveAndFlush(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isNotEqualTo(RAW_PASSWORD);
        assertThat(passwordEncoder.matches(RAW_PASSWORD, saved.getPasswordHash())).isTrue();
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.isEnabled()).isTrue();
    }

    @Test
    void shouldThrowConflictOnTakenLogin() {
        when(credentialRepo.existsByLogin("alice_1")).thenReturn(true);

        assertThatThrownBy(() -> service.register(registerDto())).isInstanceOf(ConflictException.class);

        verify(userRepo, never()).saveAndFlush(any(User.class));
    }

    @Test
    void shouldThrowConflictOnTakenEmail() {
        when(credentialRepo.existsByLogin("alice_1")).thenReturn(false);
        when(userRepo.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(registerDto())).isInstanceOf(ConflictException.class);

        verify(userRepo, never()).saveAndFlush(any(User.class));
        verify(credentialRepo, never()).saveAndFlush(any(Credential.class));
    }

    @Test
    void shouldIssueTokenOnLoginWithNormalizedLogin() {
        var user = new AuthUser(7L, "alice_1", "hash", Role.USER, true);
        var token = new TokenDto("jwt", "Bearer", 3600L);
        when(authenticationManager.authenticate(any()))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
        when(tokenService.issue(user)).thenReturn(token);

        var result = service.login(new LoginDto().setLogin("  Alice_1 ").setPassword(RAW_PASSWORD));

        assertThat(result).isSameAs(token);

        var captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("alice_1");
        assertThat(captor.getValue().getCredentials()).isEqualTo(RAW_PASSWORD);
    }

    @Test
    void shouldPropagateAuthenticationFailureWithoutIssuingToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> service.login(new LoginDto().setLogin("alice_1").setPassword("wrong")))
                .isInstanceOf(BadCredentialsException.class);

        verify(tokenService, never()).issue(any(AuthUser.class));
    }

    @Test
    void shouldThrowNotFoundOnUnknownProfile() {
        when(credentialRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(99L)).isInstanceOf(NotFoundException.class);
    }

    private static RegisterDto registerDto() {
        return new RegisterDto()
                .setLogin(" Alice_1 ")
                .setPassword(RAW_PASSWORD)
                .setName(" Alice ")
                .setEmail("Alice@Example.COM");
    }
}
