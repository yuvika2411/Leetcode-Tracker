package com.tracker.leetcode.tracker.Security;

import com.tracker.leetcode.tracker.Repository.MentorRepository;
import com.tracker.leetcode.tracker.Repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final MentorRepository mentorRepository;
    private final StudentRepository studentRepository;

    @Bean
    public UserDetailsService userDetailsService(){
        return username -> {
            var mentor = mentorRepository.findByEmail(username);
            if (mentor.isPresent()){
                return mentor.get();
            }
            var student = studentRepository.findByEmail(username);
            if (student.isPresent()){
                return student.get();
            }
            // 3. If neither, reject the login
            throw new UsernameNotFoundException("User not found with email: " + username);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 3. The Authentication Provider bundles the UserDetailsService and the PasswordEncoder together
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }


    // 4. The Authentication Manager is the actual component that processes login requests
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }
}
