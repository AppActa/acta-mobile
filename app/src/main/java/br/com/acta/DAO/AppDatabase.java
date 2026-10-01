package br.com.acta.DAO;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import br.com.acta.Model.Ciclo;
import br.com.acta.Model.Colaborador;
import br.com.acta.Model.Empresa;
import br.com.acta.Model.Me;
import br.com.acta.Model.Meta;
import br.com.acta.Model.Usuario;
import br.com.acta.Model.UsuarioCiclo;

@Database(entities = {Usuario.class, Ciclo.class, Me.class, Meta.class, Colaborador.class, Empresa.class, UsuarioCiclo.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsuarioDao usuarioDao();
    public abstract MeDao meDao();
    public abstract CicloDao cicloDao();
    public abstract ColaboradorDao colaboradorDao();
    public abstract UsuarioCicloDao usuarioCicloDao();

    private static AppDatabase INSTANCE;

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "acta_local_db"
            ).allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
}
