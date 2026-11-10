package ma.eventpro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.CategorieBilletRequest;
import ma.eventpro.dto.request.CategorieBilletUpdateRequest;
import ma.eventpro.dto.response.CategorieBilletResponse;
import ma.eventpro.service.CategorieBilletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorie" , description = "Gestion des Categories")
public class CategorieBilletController {
    private final CategorieBilletService categorieBilletService;

    @PostMapping("/{eventId}")
    public ResponseEntity<CategorieBilletResponse> createCategorie(@PathVariable Long eventId , @RequestParam UUID organisateurId,@RequestBody CategorieBilletRequest request){
        CategorieBilletResponse response = categorieBilletService.addCategorie(eventId, organisateurId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategorieBilletResponse> updateCategorie(@PathVariable Long id,@RequestParam UUID organisateurId,
                                                                   @Valid @RequestBody CategorieBilletUpdateRequest request) {
        CategorieBilletResponse response = categorieBilletService.updateCategorie(id, organisateurId, request);
        return ResponseEntity.ok(response);
    }
}
