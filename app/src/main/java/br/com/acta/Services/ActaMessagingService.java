package br.com.acta.Services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import br.com.acta.HomeActivity;
import br.com.acta.R;

public class ActaMessagingService extends FirebaseMessagingService {
    public static final String CHANNEL = "acta_notificacoes";

    public static void criarCanal(Context context) {
        NotificationChannel canal = new NotificationChannel(CHANNEL, "Notificações ACTA", NotificationManager.IMPORTANCE_DEFAULT);
        context.getSystemService(NotificationManager.class).createNotificationChannel(canal);
    }

    @Override
    public void onRegistered(String installationId) {
        new Handler(Looper.getMainLooper()).post(() -> DispositivoService.atualizarFidRegistrado(this, installationId));
    }

    @Override
    public void onMessageReceived(RemoteMessage mensagem) {
        if (!DispositivoService.habilitado(this)) return;

        String idNotificacao = mensagem.getData().get("idNotificacao");
        if (idNotificacao == null) return;

        criarCanal(this);
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

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
        notificacaoAndroid.setSmallIcon(R.drawable.sino);
        notificacaoAndroid.setContentTitle("ACTA");
        notificacaoAndroid.setContentText(notificacao != null && notificacao.getBody() != null ? notificacao.getBody() : "Uma nova notificação está disponível.");
        notificacaoAndroid.setContentIntent(abrirCentral);
        notificacaoAndroid.setAutoCancel(true);
        return notificacaoAndroid;
    }
}