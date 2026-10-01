package br.com.acta.Model;

public class AtivarRequest {
    private final String token;

    public AtivarRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}