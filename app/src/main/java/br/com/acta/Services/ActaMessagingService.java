package br.com.acta.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import br.com.acta.HomeActivity;
import br.com.acta.R;

public class ActaMessagingService extends FirebaseMessagingService {
    public static final String CHANNEL = "acta_alertas";
    private static final String CHANNEL_ANTERIOR = "acta_notificacoes";

    public static void criarCanal(Context context) {
        NotificationManager gerenciador = context.getSystemService(NotificationManager.class);
        if (gerenciador.getNotificationChannel(CHANNEL) != null) return;

        // a importância do canal antigo não pode ser aumentada pelo app
        NotificationChannel canal = new NotificationChannel(CHANNEL, "Avisos ACTA", NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription("Avisos sobre suas atividades no ACTA.");
        NotificationChannel anterior = gerenciador.getNotificationChannel(CHANNEL_ANTERIOR);
        if (anterior != null) {
            if (anterior.hasUserSetImportance()) canal.setImportance(anterior.getImportance());
            canal.setSound(anterior.getSound(), anterior.getAudioAttributes());
            canal.setVibrationPattern(anterior.getVibrationPattern());
            canal.enableVibration(anterior.shouldVibrate());
            canal.setShowBadge(anterior.canShowBadge());
        }
        gerenciador.createNotificationChannel(canal);
    }

    @Override
    public void onRegistered(String installationId) {
        new Handler(Looper.getMainLooper()).post(() -> DispositivoService.atualizarFidRegistrado(this, installationId));
    }

    @Override
    public void onMessageReceived(RemoteMessage mensagem) {
        if (!DispositivoService.habilitado(this)) return;

        String idNotificacao = mensagem.getData().get("idNotificacao");
        if (idNotificacao == null) idNotificacao = mensagem.getData().get("notificationId");
        if (idNotificacao == null) return;

        criarCanal(this);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("idNotificacao", idNotificacao);
        intent.setData(Uri.parse("acta://notificacoes/" + Uri.encode(idNotificacao)));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent abrirCentral = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificacaoAndroid = getNotificacaoAndroid(mensagem, abrirCentral);

        NotificationManagerCompat.from(this).notify(idNotificacao.hashCode(), notificacaoAndroid.build());
    }

    private NotificationCompat.Builder getNotificacaoAndroid(RemoteMessage mensagem, PendingIntent abrirCentral) {
        RemoteMessage.Notification notificacao = mensagem.getNotification();

        NotificationCompat.Builder notificacaoAndroid = new NotificationCompat.Builder(this, CHANNEL);
        notificacaoAndroid.setSmallIcon(R.drawable.ic_notificacao_acta);
        notificacaoAndroid.setColor(ContextCompat.getColor(this, R.color.turquesa_dark_center));
        notificacaoAndroid.setContentTitle(notificacao != null && notificacao.getTitle() != null ? notificacao.getTitle() : "ACTA");
        notificacaoAndroid.setContentText(notificacao != null && notificacao.getBody() != null ? notificacao.getBody() : "Uma nova notificação está disponível.");
        notificacaoAndroid.setContentIntent(abrirCentral);
        notificacaoAndroid.setAutoCancel(true);
        return notificacaoAndroid;
    }
}