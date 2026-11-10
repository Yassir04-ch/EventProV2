package ma.eventpro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.RemboursementCreateRequest;
import ma.eventpro.dto.response.RemboursementResponse;
import ma.eventpro.service.RemboursementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequestMapping("/api/remboursements")
@RequiredArgsConstructor
@Tag(name = "Remboursements" , description = "Gestion des Remboursements")
public class RemboursementController {
    private final RemboursementService remboursementService;

    @PostMapping
    public ResponseEntity<RemboursementResponse> createRemboursement(@Valid @RequestBody RemboursementCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(remboursementService.creatRemboursement(request));
    }


    @GetMapping("/{id}")
    public ResponseEntity<RemboursementResponse> getRemboursement(@PathVariable Long id){
        return ResponseEntity.ok(remboursementService.getById(id));
    }
}
