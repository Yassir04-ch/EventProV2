package ma.eventpro.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import ma.eventpro.dto.request.EventCreateRequest;
import ma.eventpro.dto.request.EventUpdateRequest;
import ma.eventpro.dto.response.EvenementResponse;
import ma.eventpro.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
@Tag(name = "Evenements" ,description = "Gestion des evenements")
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EvenementResponse> createEvent(@Valid @RequestBody EventCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<EvenementResponse> publishEvent(@PathVariable Long id){
        return ResponseEntity.ok(eventService.publishEvent(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<EvenementResponse> cancelEvent(@PathVariable Long id){
        return ResponseEntity.ok(eventService.cancelEvent(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvenementResponse> getEvent(@PathVariable Long id){
        return ResponseEntity.ok(eventService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<EvenementResponse>> getPublishedEvents() {
        return ResponseEntity.ok(eventService.getPublishedEvents());
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> annulerEvent(@PathVariable Long id , @RequestParam UUID organisateurId){
        eventService.annulerEvent(id, organisateurId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/Modifier")
    public ResponseEntity<EvenementResponse> updateEvent(@PathVariable Long id , @RequestParam UUID organisateurId , @RequestBody EventUpdateRequest request){
        return ResponseEntity.ok(eventService.updateEvent(id, organisateurId , request));
    }

    @GetMapping("/{id}/MyEvent")
    public ResponseEntity<List<EvenementResponse>> getMyEvent(@PathVariable UUID id){
        return ResponseEntity.ok(eventService.getMyEvent(id));
    }
}
