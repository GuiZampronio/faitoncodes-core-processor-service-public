package com.faitoncodes.core_processor_service.repository;

import com.faitoncodes.core_processor_service.dto.user.UsuarioDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public boolean existsByUserId(Long userId) {
        String sql = "SELECT CASE WHEN count(u) > 0 THEN true ELSE false END FROM public.usuario u where u.user_id = :userId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        return (boolean) query.getSingleResult();
    }

    public String getUserName(Long userId){
        String sql = "SELECT u.nome from public.usuario u where u.user_id = :userId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        return (String) query.getSingleResult();
    }

    public Integer getTipoUsuario(Long userId){
        String sql = "SELECT u.tipo_usuario from public.usuario u where u.user_id = :userId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        return (Integer) query.getSingleResult();
    }

    public UsuarioDTO getDadosUsuario(Long userId) {
        String sql = "SELECT u.nome, u.email, u.color FROM public.usuario u WHERE u.user_id = :userId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);

        Object[] result = (Object[]) query.getSingleResult();
        if (result != null) {
            String nome = (String) result[0];
            String email = (String) result[1];
            String cor = (String) result[2];
            return new UsuarioDTO(nome, email, cor);
        }
        return null;
    }

}
