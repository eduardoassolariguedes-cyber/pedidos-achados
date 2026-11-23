
package br.com.achadoseperdidos.controller;

import br.com.achadoseperdidos.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/cadastro")
    public String paginaCadastro() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            Model model) {

        if (!StringUtils.hasText(nome) || !StringUtils.hasText(email)
                || !StringUtils.hasText(senha)) {
            model.addAttribute("erro", "Preencha todos os campos.");
            return "cadastro";
        }

        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("erro", "As senhas não são iguais.");
            return "cadastro";
        }

        if (senha.length() < 8) {
            model.addAttribute("erro", "A senha precisa ter pelo menos 8 caracteres.");
            return "cadastro";
        }

        try {
            usuarioService.cadastrar(nome, email, senha);
            model.addAttribute("sucesso", "Conta criada com sucesso!");
            return "cadastro";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("erro", exception.getMessage());
            return "cadastro";
        }
    }
}
