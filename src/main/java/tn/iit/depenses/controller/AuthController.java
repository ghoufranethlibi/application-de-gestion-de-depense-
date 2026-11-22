package tn.iit.depenses.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tn.iit.depenses.service.AuthService;
import tn.iit.depenses.service.dto.InscriptionRequest;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping(Routes.CONNEXION)
    public String connexion() {
        return "connexion";
    }

    @GetMapping(Routes.INSCRIPTION)
    public String inscription() {
        return "inscription";
    }

    @PostMapping(Routes.INSCRIPTION)
    public String creerCompte(@Valid InscriptionRequest demande, RedirectAttributes redirect) {
        authService.inscrire(demande);
        redirect.addFlashAttribute(MessagesFlash.SUCCES, "Votre compte est créé. Vous pouvez vous connecter.");
        return Routes.rediriger(Routes.CONNEXION);
    }
}
