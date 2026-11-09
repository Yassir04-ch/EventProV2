package ma.eventpro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.PromotionCreateRequest;
import ma.eventpro.dto.response.PromotionResponse;
import ma.eventpro.service.PromotionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions" , description = "Gestion des promotions")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(request));
    }

    @GetMapping("/{code}")
    public ResponseEntity<PromotionResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(promotionService.getByCode(code));
    }
}
