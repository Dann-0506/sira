package com.academico.util;

import com.academico.model.Usuario;

/**
 * Utilería para la Gestión de Sesión.
 * Responsabilidad: Almacenar globalmente la información del usuario autenticado
 * para proveer controles de acceso (RBAC) en toda la aplicación.
 */
public class SessionManagerUtil {

    private static Usuario usuarioActual;

    // ==========================================
    // CONTROL DE CICLO DE VIDA DE LA SESIÓN
    // ==========================================

    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    // ==========================================
    // VALIDACIONES DE ACCESO Y ROLES
    // ==========================================

    public static boolean haySession() {
        return usuarioActual != null;
    }

    public static boolean esAdmin() {
        return haySession() && "admin".equals(usuarioActual.getRol());
    }

    public static boolean esMaestro() {
        return haySession() && "maestro".equals(usuarioActual.getRol());
    }
}