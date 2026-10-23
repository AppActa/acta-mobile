package br.com.acta.Model;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatAudioRequest extends RequestBody {

    private File audio;
    private String audioContentType;
    private String sessionId;
    private Integer usuarioId;
    private Integer empresaId;
    private List<Integer> idCiclo;
    private Integer cicloAtivo;

    private MultipartBody transformaEmMultipart() {
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
