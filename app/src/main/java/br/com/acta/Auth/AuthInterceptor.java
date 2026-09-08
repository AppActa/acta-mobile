package br.com.acta.Auth;

import androidx.annotation.NonNull;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthInterceptor implements Interceptor {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final TokenProvider tokenProvider;

    public AuthInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = attachToken(chain.request(), tokenProvider.getToken(false));
        Response response = chain.proceed(request);

        if (response.code() != 401 || request.tag(RefreshAttempt.class) != null) {
            return response;
        }

        String refreshedToken = tokenProvider.getToken(true);
        if (refreshedToken == null || refreshedToken.isBlank()) {
            return response;
        }

        response.close();
        Request retry = attachToken(request, refreshedToken)
                .newBuilder()
                .tag(RefreshAttempt.class, new RefreshAttempt())
                .build();
        return chain.proceed(retry);
    }

    private Request attachToken(Request request, String token) {
        if (token == null || token.isBlank()) {
            return request;
        }
        return request.newBuilder()
                .header(AUTHORIZATION, BEARER + token)
                .build();
    }

    private static final class RefreshAttempt {
    }
}
