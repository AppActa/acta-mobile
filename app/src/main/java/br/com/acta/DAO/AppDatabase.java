package br.com.acta.DAO;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import br.com.acta.Model.Ciclo;
import br.com.acta.Model.EfeitoSecundario;
import br.com.acta.Model.Colaborador;
import br.com.acta.Model.Empresa;
import br.com.acta.Model.Me;
import br.com.acta.Model.Meta;
import br.com.acta.Model.PlanoAcao;
import br.com.acta.Model.Tarefa;
import br.com.acta.Model.Usuario;
import br.com.acta.Model.UsuarioCiclo;

@Database(entities = {Usuario.class, Ciclo.class, Me.class, Meta.class, Colaborador.class, Empresa.class, UsuarioCiclo.class, PlanoAcao.class, Tarefa.class, EfeitoSecundario.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsuarioDao usuarioDao();
    public abstract MeDao meDao();
    public abstract CicloDao cicloDao();
    public abstract ColaboradorDao colaboradorDao();
    public abstract UsuarioCicloDao usuarioCicloDao();
    public abstract PlanoAcaoDao planoAcaoDao();
    public abstract TarefaDao tarefaDao();
    public abstract EfeitoSecundarioDao efeitoSecundarioDao();

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS tb_efeito_secundario ("
                    + "id INTEGER, descricao TEXT, peso TEXT, impactoEstimado TEXT, "
                    + "idVerificacaoResultado INTEGER, tipo TEXT, PRIMARY KEY(id))");
        }
    };

    private static AppDatabase INSTANCE;

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "acta_local_db"
            )
            .addMigrations(MIGRATION_5_6)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build();
        }
        return INSTANCE;
    }
}
