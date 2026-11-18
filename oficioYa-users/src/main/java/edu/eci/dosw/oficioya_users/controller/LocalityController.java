package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.dto.LocalityDTO;
import edu.eci.dosw.oficioya_users.service.LocalityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/localities")
@RequiredArgsConstructor
@Tag(name = "Localidades", description = "Catálogo de localidades de Bogotá para la zona de cobertura")
public class LocalityController {

    private final LocalityService localityService;

    @GetMapping
    @Operation(summary = "Consultar localidades", description = "RF-14. Catálogo de zonas de cobertura. No requiere token")
    public List<LocalityDTO> findAll() {
        return localityService.findAll();
    }

    @GetMapping("/{id}/workers")
    @Operation(summary = "Trabajadores de una localidad",
            description = "RF-14. Ids de las cuentas activas con rol TRABAJADOR cuya zona de cobertura es esa localidad. 404 si no existe")
    public List<UUID> findWorkerIds(@PathVariable Long id) {
        return localityService.findWorkerIds(id);
    }
}