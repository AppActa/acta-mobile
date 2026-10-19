package br.com.acta.Auth;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class FirebaseTokenProvider implements TokenProvider{
    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenProvider(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public String getToken(boolean forceRefresh) throws IOException {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            return null;
        }

        try {
            GetTokenResult result = Tasks.await(user.getIdToken(forceRefresh));
            return result.getToken();
        } catch (ExecutionException e) {
            throw new IOException("Não foi possível obter o ID Token do Firebase.", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("A obtenção do ID Token foi interrompida.", e);
        }
    }
}