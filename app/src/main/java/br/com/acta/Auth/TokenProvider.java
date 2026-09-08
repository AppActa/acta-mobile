package br.com.acta.Auth;

import java.io.IOException;

public interface TokenProvider {
    String getToken(boolean forceRefresh) throws IOException;
}
