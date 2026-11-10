package ma.eventpro.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.*;
import ma.eventpro.dto.response.UtilisateurResponse;
import ma.eventpro.entity.*;
import ma.eventpro.exception.ConflictException;
import ma.eventpro.exception.ResourceNotFoundException;
import ma.eventpro.mapper.UtilisateurMapper;
import ma.eventpro.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UtilisateurService {
    private final UserRepository userRepository;
    private final UtilisateurMapper utilisateurMapper;

    private void checkEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Cet email est déja utilisé");
        }
    }

    @Transactional
    public UtilisateurResponse createClient(ClientCreateRequest request){
       checkEmailAvailable(request.getEmail());
       Client client =  userRepository.save(utilisateurMapper.toEntity(request));
        return utilisateurMapper.toClientResponse(client);
    }

    @Transactional
    public UtilisateurResponse createOrganisateur(OrganisateurCreateRequest request){
        checkEmailAvailable(request.getEmail());
        Organisateur organisateur = userRepository.save(utilisateurMapper.toEntity(request));
        return utilisateurMapper.toOrganisateurResponse(organisateur);
    }

    @Transactional
    public UtilisateurResponse createAdministrateur(AdministrateurCreateRequest request){
        checkEmailAvailable(request.getEmail());
        Administrateur administrateur = userRepository.save(utilisateurMapper.toEntity(request));
        return utilisateurMapper.toAdministrateurResponce(administrateur);
    }

    @Transactional
    public UtilisateurResponse createAgentControle(AgentControleCreateRequest request){
        checkEmailAvailable(request.getEmail());
        AgentControle agentControle = userRepository.save(utilisateurMapper.toEntity(request));
        return utilisateurMapper.toAgentControleResponce(agentControle);
    }

    @Transactional
    public UtilisateurResponse getById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur not found"));
        if(user instanceof Client client){
            return utilisateurMapper.toClientResponse(client);
        }
        if(user instanceof Organisateur organisateur){
            return utilisateurMapper.toOrganisateurResponse(organisateur);
        }
        if(user instanceof Administrateur administrateur){
            return utilisateurMapper.toAdministrateurResponce(administrateur);
        }
        if(user instanceof AgentControle agentControle){
            return utilisateurMapper.toAgentControleResponce(agentControle);
        }
        throw new IllegalStateException("Type utilisateur non reconnu");
    }

    @Transactional
    public UtilisateurResponse updateProfile(UUID id, UtilisateurUpdateRequest request) {
        User utilisateur = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        if (request.getEmail() != null && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ConflictException("Cet émail est déja utilisé");
            }
        if (utilisateur instanceof Client client) {
            utilisateurMapper.updateUtilisateur(request, client);
            return utilisateurMapper.toClientResponse(client);
        }
        if (utilisateur instanceof Organisateur organisateur) {
            utilisateurMapper.updateUtilisateur(request, organisateur);
            return utilisateurMapper.toOrganisateurResponse(organisateur);
        }
        if (utilisateur instanceof Administrateur administrateur) {
            utilisateurMapper.updateUtilisateur(request, administrateur);
            return utilisateurMapper.toAdministrateurResponce(administrateur);
        }
        if (utilisateur instanceof AgentControle agentControle) {
            utilisateurMapper.updateUtilisateur(request, agentControle);
            return utilisateurMapper.toAgentControleResponce(agentControle);
        }
        throw new IllegalStateException("Type utilisateur non reconnu");
    }

}
