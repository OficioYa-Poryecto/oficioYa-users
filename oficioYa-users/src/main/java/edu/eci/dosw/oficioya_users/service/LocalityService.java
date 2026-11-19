package edu.eci.dosw.oficioya_users.service;

import edu.eci.dosw.oficioya_users.dto.LocalityDTO;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;
import edu.eci.dosw.oficioya_users.repository.LocalityRepository;
import edu.eci.dosw.oficioya_users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalityService {

    private final LocalityRepository localityRepository;
    private final UserRepository userRepository;

    /** RF-14. Catálogo de localidades, ordenado por id */
    public List<LocalityDTO> findAll() {
        return localityRepository.findAllByOrderByIdAsc().stream()
                .map(locality -> new LocalityDTO(locality.getId(), locality.getName()))
                .toList();
    }

    /** RF-14. Ids de las cuentas activas con rol TRABAJADOR cuya zona de cobertura es esa localidad */
    public List<UUID> findWorkerIds(Long localityId) {
        if (!localityRepository.existsById(localityId)) {
            throw new ResourceNotFoundException("Localidad no encontrada: " + localityId);
        }
        List<UUID> ids = userRepository.findIdsByLocalityAndRole(localityId, RoleName.TRABAJADOR.name(), AccountStatus.ACTIVE);
        log.info("Trabajadores en la localidad {}: {}", localityId, ids.size());
        return ids;
    }
}