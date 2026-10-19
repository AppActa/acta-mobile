package br.com.acta.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import br.com.acta.Model.Colaborador;

@Dao
public interface ColaboradorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(Colaborador colaborador);

    @Query("SELECT * FROM tb_colaborador WHERE id = :id LIMIT 1")
    Colaborador buscarPorId(Long id);
}
