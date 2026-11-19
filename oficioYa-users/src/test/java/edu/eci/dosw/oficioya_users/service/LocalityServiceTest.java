package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.LocalityDTO;
import edu.eci.dosw.oficioya_users.entity.LocalityEntity;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;
import edu.eci.dosw.oficioya_users.repository.LocalityRepository;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalityServiceTest {

    @Mock
    private LocalityRepository localityRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private LocalityService localityService;

    @Test
    void findAllReturnsCatalog() {
        LocalityEntity suba = new LocalityEntity();
        suba.setId(11L);
        suba.setName("Suba");
        when(localityRepository.findAllByOrderByIdAsc()).thenReturn(List.of(suba));

        assertThat(localityService.findAll()).containsExactly(new LocalityDTO(11L, "Suba"));
    }

    @Test
    void findWorkerIdsReturnsActiveWorkersOfLocality() {
        UUID workerId = UUID.randomUUID();
        when(localityRepository.existsById(11L)).thenReturn(true);
        when(userRepository.findIdsByLocalityAndRole(11L, "TRABAJADOR", AccountStatus.ACTIVE)).thenReturn(List.of(workerId));

        assertThat(localityService.findWorkerIds(11L)).containsExactly(workerId);
    }

    @Test
    void findWorkerIdsFailsWhenLocalityDoesNotExist() {
        when(localityRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> localityService.findWorkerIds(99L));
        verifyNoInteractions(userRepository);
    }
}