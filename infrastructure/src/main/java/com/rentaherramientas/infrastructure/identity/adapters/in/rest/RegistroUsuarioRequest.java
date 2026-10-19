package com.rentaherramientas.infrastructure.identity.adapters.in.rest;

import com.rentaherramientas.application.identity.command.RegistrarUsuarioCommand;
import com.rentaherramientas.domain.identity.model.valueobject.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "Nombre completo del usuario", example = "Laura Valentina Gómez Ardila")
        @NotBlank @Size(max = 120) String nombre,
        @Schema(description = "Email único; será el usuario de login", example = "laura.gomez@gmail.com")
        @NotBlank @Email @Size(max = 150) String email,
        @Schema(description = "Contraseña de 8 a 72 caracteres", example = "Clave123*", format = "password")
        @NotBlank @Size(min = 8, max = 72) String password,
        @Schema(description = "Rol a registrar: solo CLIENTE o PROVEEDOR", example = "CLIENTE")
        @NotNull Rol rol,
        @Schema(description = "Obligatorio si rol = PROVEEDOR; omitir en otro caso")
        @Valid DatosProveedorRequest datosProveedor,
        @Schema(description = "Obligatorio si rol = CLIENTE; omitir en otro caso")
        @Valid DatosClienteRequest datosCliente) {

    /** Datos obligatorios cuando el rol es PROVEEDOR. */
    public record DatosProveedorRequest(
            @Schema(description = "Razón social de la empresa", example = "Ferretería El Tornillo SAS")
            @NotBlank @Size(max = 150) String razonSocial,
            @Schema(description = "NIT con dígito de verificación", example = "900101001-3")
            @NotBlank @Size(max = 20) String nit) {
    }

    /** Datos obligatorios cuando el rol es CLIENTE. */
    public record DatosClienteRequest(
            @Schema(description = "Número de cédula", example = "1098654321")
            @NotBlank @Size(max = 20) String documento,
            @Schema(description = "Celular de contacto", example = "3157894561")
            @NotBlank @Size(max = 20) String telefono,
            @Schema(description = "Dirección de entrega", example = "Cra 33 # 48-25, Cabecera del Llano, Bucaramanga")
            @NotBlank @Size(max = 200) String direccion) {
    }

    /** Convierte el cuerpo HTTP en el command de aplicación. */
    public RegistrarUsuarioCommand toCommand() {
        return new RegistrarUsuarioCommand(nombre, email, password, rol,
                datosProveedor == null ? null
                        : new RegistrarUsuarioCommand.DatosProveedor(datosProveedor.razonSocial(), datosProveedor.nit()),
                datosCliente == null ? null
                        : new RegistrarUsuarioCommand.DatosCliente(datosCliente.documento(), datosCliente.telefono(),
                                datosCliente.direccion()));
    }

    /** Oculta la contraseña para que no aparezca en logs. */
    @Override
    public String toString() {
        return "RegistroUsuarioRequest[nombre=" + nombre + ", email=" + email + ", password=****, rol=" + rol + "]";
    }
}
