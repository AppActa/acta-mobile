package br.com.acta.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.com.acta.Model.Tarefa;

@Dao
public interface TarefaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(Tarefa tarefa);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarTodos(List<Tarefa> tarefas);

    @Query("SELECT * FROM tb_tarefa WHERE id = :id LIMIT 1")
    Tarefa buscarPorId(Long id);

    @Query("SELECT * FROM tb_tarefa WHERE idPlanoAcao = :idPlanoAcao")
    List<Tarefa> buscarPorPlanoAcao(Long idPlanoAcao);

    @Query("SELECT * FROM tb_tarefa")
    List<Tarefa> listarTodas();
}
