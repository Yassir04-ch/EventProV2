package ma.eventpro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.*;
import ma.eventpro.dto.response.UtilisateurResponse;
import ma.eventpro.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs" , description = "Gestion des utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PostMapping("/clients")
    public ResponseEntity<UtilisateurResponse> createClient(@Valid @RequestBody ClientCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.createClient(request));
    }

    @PostMapping("/organisateurs")
    public ResponseEntity<UtilisateurResponse> createOrganisateur(@Valid @RequestBody OrganisateurCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.createOrganisateur(request));
    }

    @PostMapping("/admins")
    public ResponseEntity<UtilisateurResponse> createAdministrateur(@Valid @RequestBody AdministrateurCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.createAdministrateur(request)) ;
    }

    @PostMapping("/agents")
    public ResponseEntity<UtilisateurResponse> createAgentControle(@Valid @RequestBody AgentControleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.createAgentControle(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> getProfile(@PathVariable UUID id){
        return ResponseEntity.ok(utilisateurService.getById(id));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UtilisateurResponse> updateProfile(@PathVariable UUID id , @Valid @RequestBody UtilisateurUpdateRequest request){
        return ResponseEntity.ok(utilisateurService.updateProfile(id,request));
    }
}