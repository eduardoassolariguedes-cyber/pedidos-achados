
package br.com.achadoseperdidos.controller;

import br.com.achadoseperdidos.model.Usuario;
import br.com.achadoseperdidos.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @GetMapping("/login")
    public String paginaLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String entrar(
            @RequestParam String email,
            @RequestParam String senha,
            HttpSession session,
            Model model) {

        Usuario usuario = usuarioRepository
                .findByEmail(email.trim().toLowerCase())
                .orElse(null);

        if (usuario == null ||
                !passwordEncoder.matches(senha, usuario.getSenha())) {

            model.addAttribute("erro", "E-mail ou senha incorretos.");
            return "login";
        }

        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioNome", usuario.getNome());

        return "redirect:/";
    }
    @GetMapping("/sair")
    public String sair(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
