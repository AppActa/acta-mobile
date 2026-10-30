package br.com.acta.Services;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/** Grava áudio AAC em M4A, sem análise de amplitude. */
public class AudioRecorder {
    private static final long LIMITE_ARQUIVO_BYTES = 24L * 1024L * 1024L;
    private static final int LIMITE_DURACAO_MS = 120_000;

    private MediaRecorder recorder;
    private File arquivo;
    private boolean gravando;

    public void iniciar(Context context, File diretorio, Runnable aoAtingirLimite)
            throws IOException {
        if (gravando) return;

        try {
            arquivo = File.createTempFile("chat_audio_", ".m4a", diretorio);
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(32_000);
            recorder.setAudioSamplingRate(16_000);
            recorder.setOutputFile(arquivo.getAbsolutePath());
            recorder.setMaxDuration(LIMITE_DURACAO_MS);
            recorder.setMaxFileSize(LIMITE_ARQUIVO_BYTES);
            recorder.setOnInfoListener((mediaRecorder, what, extra) -> {
                if ((what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
                        || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
                        && aoAtingirLimite != null) {
                    aoAtingirLimite.run();
                }
            });
            recorder.prepare();
            recorder.start();
            gravando = true;
        } catch (IOException | RuntimeException erro) {
            liberar();
            apagarArquivo();
            if (erro instanceof IOException) throw (IOException) erro;
            throw new IOException("Não foi possível iniciar a gravação.", erro);
        }
    }

    public File parar() {
        if (!gravando || recorder == null) return null;
        File arquivoGravado = arquivo;
        boolean gravacaoValida = true;
        try {
            recorder.stop();
        } catch (RuntimeException erro) {
            gravacaoValida = false;
        } finally {
            liberar();
            arquivo = null;
        }
        if (!gravacaoValida || arquivoGravado == null || !arquivoGravado.exists()
                || arquivoGravado.length() == 0) {
            if (arquivoGravado != null) arquivoGravado.delete();
            return null;
        }
        return arquivoGravado;
    }

    public void cancelar() {
        liberar();
        apagarArquivo();
    }

    public boolean estaGravando() {
        return gravando;
    }

    private void liberar() {
        if (recorder != null) {
            try {
                recorder.reset();
            } catch (RuntimeException ignored) {
            }
            recorder.release();
            recorder = null;
        }
        gravando = false;
    }

    private void apagarArquivo() {
        if (arquivo != null) arquivo.delete();
        arquivo = null;
    }
}