
package br.com.achadoseperdidos.service;

import br.com.achadoseperdidos.model.Usuario;
import br.com.achadoseperdidos.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Usuario cadastrar(String nome, String email, String senha) {

        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        }

        String senhaCriptografada = passwordEncoder.encode(senha);

        Usuario usuario = new Usuario(
                nome.trim(),
                email.trim().toLowerCase(),
                senhaCriptografada
        );

        return usuarioRepository.save(usuario);
    }
}
