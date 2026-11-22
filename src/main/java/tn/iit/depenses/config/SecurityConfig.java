package tn.iit.depenses.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import tn.iit.depenses.controller.Routes;
import tn.iit.depenses.domain.Role;
import tn.iit.depenses.repository.MembreRepository;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", Routes.INSCRIPTION, Routes.CONNEXION, "/css/**").permitAll()
                        .requestMatchers(Routes.ADMINISTRATION + "/**").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage(Routes.CONNEXION)
                        .loginProcessingUrl(Routes.CONNEXION)
                        .defaultSuccessUrl(Routes.TABLEAU_DE_BORD, true)
                        .failureUrl(Routes.CONNEXION + "?erreur"))
                .logout(logout -> logout.logoutSuccessUrl(Routes.CONNEXION + "?deconnexion"))
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
