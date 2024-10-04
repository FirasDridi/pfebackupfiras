package com.mss.adminservice.Controller;

import com.mss.adminservice.Service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class PasswordController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            // Validation du format de l'e-mail (optionnel)
            if (!isValidEmail(email)) {
                return ResponseEntity.badRequest().body("Adresse e-mail invalide");
            }

            passwordResetService.sendResetEmail(email);

            // Retourner un message générique pour des raisons de sécurité
            return ResponseEntity.ok("Si un compte est associé à cet e-mail, un e-mail de réinitialisation a été envoyé");
        } catch (Exception e) {
            // Logger l'erreur (optionnel)
            e.printStackTrace();
            return ResponseEntity.ok("Si un compte est associé à cet e-mail, un e-mail de réinitialisation a été envoyé");
        }
    }

    private boolean isValidEmail(String email) {
        // Vous pouvez utiliser une expression régulière pour valider l'e-mail
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
