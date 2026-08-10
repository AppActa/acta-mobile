package br.com.acta;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActvity extends AppCompatActivity {
    private MotionLayout btnComecar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        MotionLayout motionLayoutButton = findViewById(R.id.btnComecar);
        motionLayoutButton.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {
            }
            @Override
            public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {
            }
            public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
                if (currentId == R.id.end) {
                    Intent intent = new Intent(MainActvity.this, TelaLogin.class);
                    startActivity(intent);
                }
            }
            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {
            }
        });
    }
    @Override
    protected void onStart() {

        MotionLayout motionLayoutButton = findViewById(R.id.btnComecar);
        super.onStart();

        // Toda vez que a tela ficar visível novamente, reinicia a animação
        if (motionLayoutButton != null) {
            motionLayoutButton.setProgress(0f);      // Reseta o progresso da animação para o início
            motionLayoutButton.jumpToState(R.id.start); // Força a transição de volta para o estado inicial imediatamente
        }
    }
}