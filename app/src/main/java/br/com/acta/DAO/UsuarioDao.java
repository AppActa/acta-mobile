package br.com.acta.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import br.com.acta.Model.Usuario;

@Dao
public interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(Usuario usuario);

    // Busca o perfil do usuário logado no celular
    @Query("SELECT * FROM usuario_sistema WHERE id = :idUsuario LIMIT 1")
    Usuario buscarPorId(Long idUsuario);
}
