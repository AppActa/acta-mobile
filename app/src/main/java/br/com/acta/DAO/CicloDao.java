package br.com.acta.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.com.acta.Model.Ciclo;

@Dao
public interface CicloDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(Ciclo ciclo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarTodos(List<Ciclo> ciclos);

    @Query("DELETE FROM tb_ciclo")
    void limparTabela();

    @Query("SELECT * FROM tb_ciclo WHERE id = :id LIMIT 1")
    Ciclo buscarPorId(Long id);

    @Query("SELECT * FROM tb_ciclo")
    List<Ciclo> listarTodos();
}
