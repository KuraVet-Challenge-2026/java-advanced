package br.com.fiap.kuravet.security;

import br.com.fiap.kuravet.enums.Perfil;
import br.com.fiap.kuravet.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapta {@link Usuario} para o contrato do Spring Security, expondo tambem
 * o perfil e o idTutor associado para uso direto nos services (via
 * {@code @AuthenticationPrincipal}), sem precisar consultar o banco de novo.
 */
public class UsuarioPrincipal implements UserDetails {

    private final Long idUsuario;
    private final String username;
    private final String senha;
    private final Perfil perfil;
    private final Long idTutor;

    public UsuarioPrincipal(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.username = usuario.getUsername();
        this.senha = usuario.getSenha();
        this.perfil = usuario.getPerfil();
        this.idTutor = usuario.getTutor() != null ? usuario.getTutor().getIdTutor() : null;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Long getIdTutor() {
        return idTutor;
    }

    public boolean isTutor() {
        return perfil == Perfil.TUTOR;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
