
package org.ldv.chatbot.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    // Encodage des mots de passe
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    // Configuration des règles de sécurité
    @Bean
    fun securityFilterChain(
        http: HttpSecurity
    ): SecurityFilterChain {

        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/vendor/**"
                    ).permitAll()

                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")

                    .anyRequest()
                    .authenticated()
            }

            // Connexion
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/", false)
                    .failureUrl("/login?error")
                    .permitAll()
            }

            // Déconnexion
            .logout { logout ->
                logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            }

        return http.build()
    }
}
