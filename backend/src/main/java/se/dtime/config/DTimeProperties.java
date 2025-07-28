package se.dtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.LocalDate;

@ConfigurationProperties(prefix = "dtime")
public class DTimeProperties {
    
    private final System system;
    private final Mail mail;
    
    public DTimeProperties(System system, Mail mail) {
        this.system = system;
        this.mail = mail;
    }
    
    public System getSystem() {
        return system;
    }
    
    public Mail getMail() {
        return mail;
    }
    
    public static class System {
        private final LocalDate startDate;
        
        public System(LocalDate startDate) {
            this.startDate = startDate;
        }
        
        public LocalDate getStartDate() {
            return startDate;
        }
    }
    
    public static class Mail {
        private final String username;
        private final String password;
        private final String host;
        private final int port;
        private final boolean debug;
        
        public Mail(String username, String password, String host, int port, boolean debug) {
            this.username = username;
            this.password = password;
            this.host = host;
            this.port = port;
            this.debug = debug;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public String getHost() {
            return host;
        }
        
        public int getPort() {
            return port;
        }
        
        public boolean isDebug() {
            return debug;
        }
    }
}