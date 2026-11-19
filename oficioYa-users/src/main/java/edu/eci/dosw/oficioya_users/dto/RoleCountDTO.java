package edu.eci.dosw.oficioya_users.dto;

/**
 * Cantidad de cuentas en total y por rol. Una cuenta con ambos roles cuenta en los dos,
 * por eso el total puede ser menor que la suma de trabajador y contratante.
 */
public record RoleCountDTO(long total, long trabajador, long contratante) {
}
