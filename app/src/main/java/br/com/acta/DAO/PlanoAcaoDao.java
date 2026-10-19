package br.com.acta.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.com.acta.Model.PlanoAcao;

@Dao
public interface PlanoAcaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(PlanoAcao planoAcao);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarTodos(List<PlanoAcao> planosAcao);

    @Query("SELECT * FROM tb_plano_acao WHERE id = :id LIMIT 1")
    PlanoAcao buscarPorId(Long id);

    @Query("SELECT * FROM tb_plano_acao WHERE idCiclo = :idCiclo")
    List<PlanoAcao> buscarPorCiclo(Long idCiclo);

    @Query("SELECT * FROM tb_plano_acao")
    List<PlanoAcao> listarTodos();
}
