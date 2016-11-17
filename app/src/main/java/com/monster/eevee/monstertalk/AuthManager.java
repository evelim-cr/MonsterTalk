package com.monster.eevee.monstertalk;

public class AuthManager {
    private static AuthManager ourInstance = new AuthManager();
    private User authenticatedUser;

    private AuthManager() {
    }

    public static AuthManager getInstance() {
        return ourInstance;
    }

    public boolean isAuthenticated() {
        return (this.authenticatedUser != null);
    }

    public User getAuthenticatedUser() {
        return this.authenticatedUser;
    }

    public boolean authenticate(String username, String password) {
        // comunica com o servidor
        //  o servidor testa se a senha esta correta
        //  o servidor retorna todas as info do usuario

        this.authenticatedUser = new User();
        this.authenticatedUser.setUsername(username);

        return true;
    }
}
