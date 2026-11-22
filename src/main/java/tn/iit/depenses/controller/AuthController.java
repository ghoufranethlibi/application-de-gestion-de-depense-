package tn.iit.depenses.controller;

import tn.iit.depenses.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @GetMapping("/connexion")
    public String connexion() { return "connexion"; }

    @GetMapping("/inscription")
    public String inscription() { return "inscription"; }

    @PostMapping("/inscription")
    public String creerCompte(@RequestParam String nom, @RequestParam String email,
                              @RequestParam String motDePasse, RedirectAttributes redirect) {
        try {
            authService.inscrire(nom, email, motDePasse);
            redirect.addFlashAttribute("succes", "Votre compte est créé. Vous pouvez vous connecter.");
            return "redirect:/connexion";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("erreur", ex.getMessage());
            return "redirect:/inscription";
        }
    }
}
