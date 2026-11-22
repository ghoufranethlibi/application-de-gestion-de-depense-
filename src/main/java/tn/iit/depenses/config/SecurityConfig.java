package tn.iit.depenses.config;

import tn.iit.depenses.repository.MembreRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/inscription", "/connexion", "/css/**").permitAll()
                        .requestMatchers("/administration/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/connexion")
                        .loginProcessingUrl("/connexion")
                        .defaultSuccessUrl("/tableau-de-bord", true)
                        .failureUrl("/connexion?erreur"))
                .logout(logout -> logout.logoutSuccessUrl("/connexion?deconnexion"))
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(MembreRepository membres) {
        return email -> membres.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Compte introuvable"));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
