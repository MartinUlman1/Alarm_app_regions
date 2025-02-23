package ivr.alarmregions.authentication;

import ivr.alarmregions.controller.PragueController;
import ivr.alarmregions.controller.RegionsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AuthenticationConfiguration {

    @Autowired
    private IConfService confService;

    @Bean
    public GenesysUserDetailsProvider genesysUserDetailsProvider() {
        return new GenesysUserDetailsProvider(confService);
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http, RegionsController regionsController) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/accessDenied").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/departments").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/log/view/**").permitAll()
                        .requestMatchers("/", "/index").authenticated()
                        .requestMatchers("/region/praha/**").hasAnyAuthority(PragueController.AUTHORITY, "ADMIN")
                        .requestMatchers("/region/stredocesky/**").hasAnyAuthority(RegionsController.AUTHORITY, "ADMIN")
                        .requestMatchers("/region/karlovarsky/**").hasAnyAuthority(RegionsController.AUTHORITY1, "ADMIN")
                        .requestMatchers("/region/kralovehradecky/**").hasAnyAuthority(RegionsController.AUTHORITY2, "ADMIN"))
                .exceptionHandling(exception -> exception.accessDeniedPage("/accessDenied"))
                .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/"))
                .logout(logout -> logout.deleteCookies("KHSESSIONID"))
                .build();
    }
}
