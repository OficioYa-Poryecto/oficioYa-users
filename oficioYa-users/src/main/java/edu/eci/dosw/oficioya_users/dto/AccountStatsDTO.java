package edu.eci.dosw.oficioya_users.dto;

/**
 * RF-12. Conteos para Reporting: cuentas registradas hasta la fecha de fin (activas o no)
 * y cuentas desactivadas dentro del periodo
 */
public record AccountStatsDTO(RoleCountDTO registered, RoleCountDTO deactivated) {
}
