package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.command.RegistrarUsuarioCommand;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/auth/registro. Solo valida forma (campos presentes,
 * longitudes); las reglas de negocio (rol permitido, coherencia rol ↔ datos,
 * formatos de NIT/documento) las aplican el caso de uso y el dominio.
 * El password se limita a 72 caracteres porque BCrypt ignora lo que sobra.
 */
public record RegistroUsuarioRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull Rol rol,
        @Valid DatosProveedorRequest datosProveedor,
        @Valid DatosClienteRequest datosCliente) {

    public record DatosProveedorRequest(
            @NotBlank @Size(max = 150) String razonSocial,
            @NotBlank @Size(max = 20) String nit) {
    }

    public record DatosClienteRequest(
            @NotBlank @Size(max = 20) String documento,
            @NotBlank @Size(max = 20) String telefono,
            @NotBlank @Size(max = 200) String direccion) {
    }

    public RegistrarUsuarioCommand toCommand() {
        return new RegistrarUsuarioCommand(nombre, email, password, rol,
                datosProveedor == null ? null
                        : new RegistrarUsuarioCommand.DatosProveedor(datosProveedor.razonSocial(), datosProveedor.nit()),
                datosCliente == null ? null
                        : new RegistrarUsuarioCommand.DatosCliente(datosCliente.documento(), datosCliente.telefono(),
                                datosCliente.direccion()));
    }

    @Override
    public String toString() {
        return "RegistroUsuarioRequest[nombre=" + nombre + ", email=" + email + ", password=****, rol=" + rol + "]";
    }
}
