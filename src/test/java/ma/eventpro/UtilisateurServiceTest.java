package ma.eventpro;

import ma.eventpro.dto.request.AdministrateurCreateRequest;
import ma.eventpro.dto.request.ClientCreateRequest;
import ma.eventpro.dto.request.OrganisateurCreateRequest;
import ma.eventpro.dto.response.UtilisateurResponse;
import ma.eventpro.entity.Administrateur;
import ma.eventpro.entity.Client;
import ma.eventpro.entity.Organisateur;
import ma.eventpro.exception.ConflictException;
import ma.eventpro.mapper.UtilisateurMapper;
import ma.eventpro.repository.UserRepository;
import ma.eventpro.service.UtilisateurService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UtilisateurServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UtilisateurMapper utilisateurMapper;

    @InjectMocks
    private UtilisateurService utilisateurService;

    @Test
    void createClient(){
        ClientCreateRequest request = ClientCreateRequest.builder()
                .nom("yassir")
                .email("yassir@gmail.com")
                .phone("0612345678")
                .build();

        Client client = new Client();
        client.setNom("yassir");
        client.setEmail("yassir@gmail.com");

        UUID id = UUID.randomUUID();
        Client saved = new Client();
        saved.setId(id);
        saved.setNom("yassir");
        saved.setEmail("yassir@gmail.com");

        UtilisateurResponse response = UtilisateurResponse.builder()
                .id(id)
                .nom("yassir")
                .email("yassir@gmail.com")
                .role("CLIENT")
                .build();

        when(userRepository.existsByEmail("yassir@gmail.com")).thenReturn(false);
        when(utilisateurMapper.toEntity(request)).thenReturn(client);
        when(userRepository.save(client)).thenReturn(saved);
        when(utilisateurMapper.toClientResponse(saved)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.createClient(request);

        assertEquals(id, result.getId());
        assertEquals("CLIENT", result.getRole());
        verify(userRepository).save(client);

    }

    @Test
    void createClientConflictException() {
        ClientCreateRequest request = ClientCreateRequest.builder()
                .nom("yassir")
                .email("yassir@gmail.com")
                .build();
        when(userRepository.existsByEmail("yassir@gmail.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> utilisateurService.createClient(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createOrganisateur(){
        OrganisateurCreateRequest request = OrganisateurCreateRequest.builder()
                .nom("yassir")
                .email("yassir@gmail.com")
                .organisationName("organisateurName")
                .build();

        Organisateur organisateur = new Organisateur();
        organisateur.setNom("yassir");
        organisateur.setEmail("yassir@gmail.com");
        organisateur.setOrganisationName("organisateurName");

        UUID id = UUID.randomUUID();

        Organisateur saved = new  Organisateur();
        saved.setId(id);
        saved.setNom("yassir");
        saved.setEmail("yassir@gmail.com");
        saved.setOrganisationName("organisateurName");

        UtilisateurResponse response = UtilisateurResponse.builder()
                .id(id)
                .nom("yassir")
                .email("yassir@gmail.com")
                .organisationName("organisateurName")
                .role("ORGANISATEUR")
                .build();

        when(userRepository.existsByEmail("yassir@gmail.com")).thenReturn(false);
        when(utilisateurMapper.toEntity(request)).thenReturn(organisateur);
        when(userRepository.save(organisateur)).thenReturn(saved);
        when(utilisateurMapper.toOrganisateurResponse(saved)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.createOrganisateur(request);

        assertEquals(id , result.getId());
        assertEquals("ORGANISATEUR", result.getRole());
        verify(userRepository).save(organisateur);
    }

    @Test
    void createAdministrateurConflictException() {
        OrganisateurCreateRequest request = OrganisateurCreateRequest.builder()
                .nom("yassir")
                .email("yassir@gmail.com")
                .build();
        when(userRepository.existsByEmail("yassir@gmail.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> utilisateurService.createOrganisateur(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createAdmin(){
        AdministrateurCreateRequest request = AdministrateurCreateRequest.builder()
                .nom("yassir")
                .email("yassir@gmail.com")
                .build();

        Administrateur administrateur = new Administrateur();
        administrateur.setNom("yassir");
        administrateur.setEmail("yassir@gmail.com");

        UUID id = UUID.randomUUID();

        Administrateur saved = new Administrateur();
        administrateur.setId(id);
        administrateur.setNom("yassir");
        administrateur.setEmail("yassir@gmail.com");

        UtilisateurResponse response = UtilisateurResponse.builder()
                .id(id)
                .nom("yassir")
                .email("yassir@gmail.com")
                .organisationName("organisateurName")
                .role("ADMINISTRATEUR")
                .build();

        when(userRepository.existsByEmail("yassir@gmail.com")).thenReturn(false);
        when(utilisateurMapper.toEntity(request)).thenReturn(administrateur);
        when(userRepository.save(administrateur)).thenReturn(saved);
        when(utilisateurMapper.toAdministrateurResponce(saved)).thenReturn(response);

        UtilisateurResponse result = utilisateurService.createAdministrateur(request);

        assertEquals(id , result.getId());
        assertEquals("ADMINISTRATEUR", result.getRole());
        verify(userRepository).save(administrateur);
    }
}
