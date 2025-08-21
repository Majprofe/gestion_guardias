package com.FranGarcia.NuevaVersionGuardias.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "supabase")
@Data
public class SupabaseConfig {
    private String url;
    private String anonKey;
    private String serviceRoleKey;
}
