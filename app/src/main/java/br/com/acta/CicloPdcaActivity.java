package br.com.acta;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class CicloPdcaActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ciclo_pdca);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainCicloPdca), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerPdca);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                atualizarMenuAtivo(destination.getId());
            });
        }

        View btnMenuGeral = findViewById(R.id.btnMenuGeral);
        if (btnMenuGeral != null) {
            btnMenuGeral.setOnClickListener(v -> {
                if (navController != null && navController.getCurrentDestination() != null) {
                    if (navController.getCurrentDestination().getId() != R.id.geralCicloFragment) {
                        navController.navigate(R.id.geralCicloFragment);
                    }
                }
            });
        }

        View btnMenuPlan = findViewById(R.id.btnMenuPlan);
        if (btnMenuPlan != null) {
            btnMenuPlan.setOnClickListener(v -> {
                if (navController != null && navController.getCurrentDestination() != null) {
                    if (navController.getCurrentDestination().getId() != R.id.detalhesCicloFragment) {
                        navController.navigate(R.id.detalhesCicloFragment);
                    }
                }
            });
        }
    }

    private void atualizarMenuAtivo(int destinationId) {
        View containerGeral = findViewById(R.id.containerGeral);
        View containerPlan = findViewById(R.id.containerPlan);
        ImageView imgGeral = findViewById(R.id.imgGeral);
        TextView txtGeral = findViewById(R.id.txtGeral);
        ImageView imgPlan = findViewById(R.id.imgPlan);
        TextView txtPlan = findViewById(R.id.txtPlan);

        if (destinationId == R.id.geralCicloFragment) {
            if (containerGeral != null) containerGeral.setBackgroundResource(R.drawable.bg_menu_active_circle);
            if (containerPlan != null) containerPlan.setBackground(null);
            if (imgGeral != null) imgGeral.setColorFilter(Color.parseColor("#083248"));
            if (txtGeral != null) txtGeral.setTextColor(Color.parseColor("#083248"));
            if (imgPlan != null) imgPlan.setColorFilter(Color.parseColor("#212121"));
            if (txtPlan != null) txtPlan.setTextColor(Color.parseColor("#212121"));
        } else if (destinationId == R.id.detalhesCicloFragment) {
            if (containerPlan != null) containerPlan.setBackgroundResource(R.drawable.bg_menu_active_circle);
            if (containerGeral != null) containerGeral.setBackground(null);
            if (imgPlan != null) imgPlan.setColorFilter(Color.parseColor("#083248"));
            if (txtPlan != null) txtPlan.setTextColor(Color.parseColor("#083248"));
            if (imgGeral != null) imgGeral.setColorFilter(Color.parseColor("#212121"));
            if (txtGeral != null) txtGeral.setTextColor(Color.parseColor("#212121"));
        }
    }
}
