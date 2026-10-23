package br.com.acta.Model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

@Getter
public class ChatAudioRequest extends RequestBody {

    private final File audio;
    private final String audioContentType;
    private final String sessionId;
    private final Integer usuarioId;
    private final Integer empresaId;
    private final List<Integer> idCiclo;
    private final Integer cicloAtivo;
    @Getter(AccessLevel.NONE)
    private volatile MultipartBody multipartBody;

    public ChatAudioRequest(
            File audio,
            String audioContentType,
            String sessionId,
            Integer usuarioId,
            Integer empresaId,
            List<Integer> idCiclo,
            Integer cicloAtivo
    ) {
        this.audio = audio;
        this.audioContentType = audioContentType;
        this.sessionId = sessionId;
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.idCiclo = idCiclo == null ? null : new ArrayList<>(idCiclo);
        this.cicloAtivo = cicloAtivo;
    }

    private MultipartBody transformaEmMultipart() {
        MultipartBody body = multipartBody;
        if (body != null) {
            return body;
        }
        synchronized (this) {
            if (multipartBody == null) {
                multipartBody = criarMultipartBody();
            }
            return multipartBody;
        }
    }

    private MultipartBody criarMultipartBody() {
        Objects.requireNonNull(audio, "audio é obrigatório");
        Objects.requireNonNull(sessionId, "sessionId é obrigatório");
        Objects.requireNonNull(usuarioId, "usuarioId é obrigatório");
        Objects.requireNonNull(empresaId, "empresaId é obrigatório");

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        MediaType tipoAudio = MediaType.parse(
                audioContentType != null ? audioContentType : "application/octet-stream"
        );
        builder.addFormDataPart(
                "audio",
                audio.getName(),
                RequestBody.create(tipoAudio, audio)
        );
        addTextPart(builder, "session_id", sessionId);
        addTextPart(builder, "usuario_id", String.valueOf(usuarioId));
        addTextPart(builder, "empresa_id", String.valueOf(empresaId));
        if (idCiclo != null) {
            for (Integer cicloId : idCiclo) {
                if (cicloId != null) {
                    addTextPart(builder, "id_ciclo", String.valueOf(cicloId));
                }
            }
        }
        if (cicloAtivo != null) {
            addTextPart(builder, "ciclo_ativo", String.valueOf(cicloAtivo));
        }

        return builder.build();
    }

    @Override
    public MediaType contentType() {
        return transformaEmMultipart().contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return transformaEmMultipart().contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        transformaEmMultipart().writeTo(sink);
    }

    private static void addTextPart(MultipartBody.Builder builder, String name, String value) {
        builder.addFormDataPart(name, value);
    }
}
