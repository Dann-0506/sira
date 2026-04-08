package com.academico.core.util;

import com.academico.auth.Usuario;

public class SessionManager {

    private static Usuario usuarioActual;

    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

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