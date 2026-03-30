package com.tracker.leetcode.tracker.Service;

import com.tracker.leetcode.tracker.DTO.AuthenticationRequest;
import com.tracker.leetcode.tracker.DTO.AuthenticationResponse;
import com.tracker.leetcode.tracker.DTO.RegisterRequest;
import com.tracker.leetcode.tracker.DTO.StudentRegisterRequest;
import com.tracker.leetcode.tracker.Exception.DuplicateMentorException;
import com.tracker.leetcode.tracker.Models.*;
import com.tracker.leetcode.tracker.Repository.MentorRepository;
import com.tracker.leetcode.tracker.Repository.StudentRepository;
import com.tracker.leetcode.tracker.Security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final StudentService studentService;

    // this helper method packages the Access Token and the raw Refresh Token string.
    private AuthenticationResponse buildAuthResponse(Mentor mentor){
        String jwtToken = jwtService.generateToken(mentor);

        // Delete any old refresh tokens to enforce a single device session (Optional but secure)
        refreshTokenService.deleteByMentorId(mentor.getId());

        // Generate a fresh one
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(mentor.getId());

        // We will temporarily pass the refresh token string out via the DTO so the Controller can bake it into a Cookie
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .mentorId(mentor.getId())
                .name(mentor.getName())
                .build();
    }

    public AuthenticationResponse register(RegisterRequest request){
        log.info("Registering new Mentor with email: {}",request.email());
        if (mentorRepository.findByEmail(request.email()).isPresent()){
            throw new DuplicateMentorException("Email already in use.");
        }
        Mentor mentor = new Mentor();
        mentor.setName(request.name());
        mentor.setEmail(request.email());
        mentor.setPassword(passwordEncoder.encode(request.password()));
        mentor.setRole(Role.MENTOR);
        mentor.setProvider(AuthProvider.LOCAL);
        mentor.setEnabled(true);
        Mentor savedMentor = mentorRepository.save(mentor);
        return buildAuthResponse(savedMentor);
    }

    //Login flow
    public AuthenticationResponse authenticate(AuthenticationRequest request){

        log.info("Authenticating mentor: {}",request.email());

        // This built-in Spring component verifies the email and raw password against the BCrypt hash in the DB
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),request.password())
        );

        // If we reach this line, the password was correct. Fetch the user.
        Mentor mentor = mentorRepository.findByEmail(request.email())
                .orElseThrow(); // We know they exist because authenticationManager just checked

        return buildAuthResponse(mentor);
    }


    public AuthenticationResponse refreshToken(String requestRefreshToken){
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getMentorId)
                .map(mentorId -> {
                    Mentor mentor = mentorRepository.findById(mentorId)
                            .orElseThrow(() -> new RuntimeException("Mentor not Found"));
                    return buildAuthResponse(mentor);
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    public AuthenticationResponse registerStudent(StudentRegisterRequest request){
        log.info("Registering new student: {}",request.email());
        if (studentRepository.findByEmail(request.email()).isPresent()){
            throw new RuntimeException("Student email already in use.");
        }
        Student student = new Student();
        student.setName(request.name());
        student.setEmail(request.email());
        student.setPassword(passwordEncoder.encode(request.password()));
        student.setLeetcodeUsername(request.leetcodeUsername());
        student.setRole(Role.STUDENT);
        student.setAuthProvider(AuthProvider.LOCAL);
        student.setEnabled(true);

        Student savedStudent = studentRepository.save(student);

        try {
            log.info("Auto-syncing LeetCode data for new student: {}", savedStudent.getLeetcodeUsername());
            // This will reach out to LeetCode, fetch all the data, and update the document we just saved!
            studentService.syncAllProfileData(savedStudent.getLeetcodeUsername());
        } catch (Exception e) {
            // We catch the error so the registration doesn't fail if LeetCode's API is down
            // or if the student made a typo in their username.
            log.warn("Failed to auto-sync LeetCode data for {}. They can manually sync later. Error: {}",
                    savedStudent.getLeetcodeUsername(), e.getMessage());
        }

        // 3. Generate the security tokens
        String jwtToken = jwtService.generateToken(savedStudent);

        // Delete any old refresh tokens (just in case) and generate a new one
        refreshTokenService.deleteByMentorId(savedStudent.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedStudent.getId());

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .mentorId(savedStudent.getId())
                .name(savedStudent.getName())
                .build();

    }

}
