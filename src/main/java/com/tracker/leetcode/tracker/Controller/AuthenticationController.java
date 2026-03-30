package com.tracker.leetcode.tracker.Controller;


import com.tracker.leetcode.tracker.DTO.AuthenticationRequest;
import com.tracker.leetcode.tracker.DTO.AuthenticationResponse;
import com.tracker.leetcode.tracker.DTO.RegisterRequest;
import com.tracker.leetcode.tracker.DTO.StudentRegisterRequest;
import com.tracker.leetcode.tracker.Service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*",allowCredentials = "true") // allowCredentials is required for cookies!
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    // Helper to build the secure cookie
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true); // Prevents JS access (XSS protection)
        cookie.setSecure(false);  // Set to TRUE in production when using HTTPS!
        cookie.setPath("/api/v1/auth/refresh"); // Cookie is ONLY sent to the refresh endpoint
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
        response.addCookie(cookie);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request,HttpServletResponse response){

        AuthenticationResponse authenticationResponse = authenticationService.register(request);
        setRefreshTokenCookie(response, authenticationResponse.refreshToken());

        // Return everything EXCEPT the refresh token in the JSON body (it's in the cookie now!)
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(authenticationResponse.accessToken())
                .mentorId(authenticationResponse.mentorId())
                .name(authenticationResponse.name())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request,
            HttpServletResponse response) {
        AuthenticationResponse authResponse = authenticationService.authenticate(request);
        setRefreshTokenCookie(response,authResponse.refreshToken());
        return ResponseEntity.ok(AuthenticationResponse
                .builder()
                .accessToken(authResponse.accessToken())
                .mentorId(authResponse.mentorId())
                .name(authResponse.name())
                .build());
    }
    // NEW: The Refresh Endpoint
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(401).build(); // No cookie, no refresh!
        }

        // Rotate the tokens
        AuthenticationResponse authResponse = authenticationService.refreshToken(refreshToken);

        // Set the NEW rotated refresh token in the cookie
        setRefreshTokenCookie(response, authResponse.refreshToken());

        // Return the NEW access token
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(authResponse.accessToken())
                .mentorId(authResponse.mentorId())
                .name(authResponse.name())
                .build());
    }

    // NEW: Logout Endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // To log out, we simply overwrite the cookie with an immediate expiration date
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true for HTTPS
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge(0); // Deletes the cookie
        response.addCookie(cookie);

        // Note: You should also call refreshTokenService.deleteByMentorId(...) here if you have the user context!

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register/student")
    public ResponseEntity<AuthenticationResponse> registerStudent(
            @RequestBody StudentRegisterRequest request,
            HttpServletResponse response){
        AuthenticationResponse authResponse = authenticationService.registerStudent(request);
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(authResponse.accessToken())
                .mentorId(authResponse.mentorId())
                .name(authResponse.name())
                .build());
    }
}
