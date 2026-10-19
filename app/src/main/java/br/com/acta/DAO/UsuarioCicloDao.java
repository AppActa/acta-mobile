package br.com.acta.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import br.com.acta.Model.UsuarioCiclo;

@Dao
public interface UsuarioCicloDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(UsuarioCiclo usuarioCiclo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarTodos(List<UsuarioCiclo> usuarioCiclos);

    @Query("SELECT * FROM tb_usuario_ciclo")
    List<UsuarioCiclo> listarTodos();

    @Query("SELECT * FROM tb_usuario_ciclo WHERE idUsuario = :idUsuario")
    List<UsuarioCiclo> buscarPorIdUsuario(Long idUsuario);

    @Query("SELECT * FROM tb_usuario_ciclo WHERE idCiclo = :idCiclo")
    List<UsuarioCiclo> buscarPorIdCiclo(Long idCiclo);
}
