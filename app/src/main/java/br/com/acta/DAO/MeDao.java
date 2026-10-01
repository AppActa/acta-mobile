package br.com.acta.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import br.com.acta.Model.Me;

@Dao
public interface MeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(Me me);

    // Busca o perfil do usuário logado no celular
    @Query("SELECT * FROM tb_me WHERE idUsuario = :idUsuario LIMIT 1")
    Me buscarPorId(Long idUsuario);
}
