package br.com.acta.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.com.acta.Model.EfeitoSecundario;

@Dao
public interface EfeitoSecundarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(EfeitoSecundario efeitoSecundario);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarTodos(List<EfeitoSecundario> efeitosSecundarios);

    @Query("SELECT * FROM tb_efeito_secundario WHERE id = :id LIMIT 1")
    EfeitoSecundario buscarPorId(Long id);

    @Query("SELECT * FROM tb_efeito_secundario WHERE idVerificacaoResultado = :idResultado")
    List<EfeitoSecundario> buscarPorVerificacaoResultado(Long idResultado);

    @Query("SELECT * FROM tb_efeito_secundario")
    List<EfeitoSecundario> listarTodos();
}
