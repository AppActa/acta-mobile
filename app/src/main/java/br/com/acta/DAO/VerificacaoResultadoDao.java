package br.com.acta.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.com.acta.Model.VerificacaoResultado;

@Dao
public interface VerificacaoResultadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(VerificacaoResultado verificacaoResultado);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarTodos(List<VerificacaoResultado> verificacoes);

    @Query("SELECT * FROM tb_verificacao_resultado WHERE id = :id LIMIT 1")
    VerificacaoResultado buscarPorId(Long id);

    @Query("SELECT * FROM tb_verificacao_resultado WHERE idCiclo = :idCiclo")
    List<VerificacaoResultado> buscarPorCiclo(Long idCiclo);

    @Query("SELECT * FROM tb_verificacao_resultado")
    List<VerificacaoResultado> listarTodos();
}
