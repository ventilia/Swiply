package com.swiply.backend.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.swiply.backend.admin.AdminUserDetailsService
import com.swiply.backend.auth.JwtAuthFilter
import com.swiply.backend.common.ApiErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val objectMapper: ObjectMapper,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    @Bean
    @Order(1)
    fun adminFilterChain(
        http: HttpSecurity,
        adminUserDetailsService: AdminUserDetailsService,
    ): SecurityFilterChain {
        http
            .securityMatcher("/admin/**")
            .userDetailsService(adminUserDetailsService)
            .authorizeHttpRequests {
                it.requestMatchers("/admin/login").permitAll()
                    .anyRequest().hasAnyRole("ADMIN", "MODERATOR")
            }
            .formLogin {
                it.loginPage("/admin/login")
                    .loginProcessingUrl("/admin/login")
                    .defaultSuccessUrl("/admin/dashboard", true)
                    .failureUrl("/admin/login?error")
                    .permitAll()
            }
            .logout {
                it.logoutUrl("/admin/logout")
                    .logoutSuccessUrl("/admin/login?logout")
            }
        return http.build()
    }


    @Bean
    @Order(2)
    fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/**", "/ws/**")
            .csrf { it.disable() }
            .cors(Customizer.withDefaults())
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {

                it.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ASYNC).permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()

                    .requestMatchers("/ws/**").permitAll()
                    .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "MODERATOR")
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(restAuthenticationEntryPoint())
                    .accessDeniedHandler(restAccessDeniedHandler())
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }


    @Bean
    @Order(3)
    fun publicFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/info",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/error",
                ).permitAll()
                    .anyRequest().denyAll()
            }
        return http.build()
    }

    private fun restAuthenticationEntryPoint() =
        AuthenticationEntryPoint { request: HttpServletRequest, response: HttpServletResponse, _ ->
            writeError(request, response, 401, "UNAUTHORIZED", "Требуется аутентификация")
        }

    private fun restAccessDeniedHandler() =
        AccessDeniedHandler { request: HttpServletRequest, response: HttpServletResponse, _ ->
            writeError(request, response, 403, "FORBIDDEN", "Доступ запрещён")
        }

    private fun writeError(
        request: HttpServletRequest,
        response: HttpServletResponse,
        status: Int,
        code: String,
        message: String,
    ) {
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        val body = ApiErrorResponse(status = status, code = code, message = message, path = request.requestURI)
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}
