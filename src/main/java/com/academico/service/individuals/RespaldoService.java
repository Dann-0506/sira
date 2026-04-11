package com.academico.service.individuals;

import com.academico.model.Respaldo;
import com.academico.util.BackupUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RespaldoService {

    private final String CARPETA_RESPALDOS = "respaldos_db";

    public RespaldoService() {
        File directorio = new File(CARPETA_RESPALDOS);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
    }

    public List<Respaldo> listarRespaldos() throws Exception {
        List<Respaldo> lista = new ArrayList<>();
        File directorio = new File(CARPETA_RESPALDOS);
        File[] archivos = directorio.listFiles((dir, name) -> name.endsWith(".backup") || name.endsWith(".sql"));

        if (archivos != null) {
            for (File file : archivos) {
                Path path = Paths.get(file.getAbsolutePath());
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                
                LocalDateTime fecha = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
                String tamanoMb = String.format("%.2f MB", file.length() / (1024.0 * 1024.0));
                
                lista.add(new Respaldo(file.getName(), file.getAbsolutePath(), fecha, tamanoMb));
            }
        }
        
        // Ordenar del más reciente al más antiguo
        lista.sort((r1, r2) -> r2.getFechaCreacion().compareTo(r1.getFechaCreacion()));
        return lista;
    }

    public void crearRespaldo() throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreArchivo = "respaldo_" + timestamp + ".backup";
        String rutaDestino = CARPETA_RESPALDOS + File.separator + nombreArchivo;

        BackupUtil.crearRespaldoAuto(rutaDestino);
    }

    public void restaurarRespaldo(String rutaArchivo) throws Exception {
        BackupUtil.restaurarRespaldoAuto(rutaArchivo);
    }

    public void eliminarRespaldo(String rutaArchivo) throws Exception {
        File archivo = new File(rutaArchivo);
        if (archivo.exists()) {
            if (!archivo.delete()) {
                throw new Exception("No se pudo eliminar el archivo de respaldo físico.");
            }
        } else {
            throw new Exception("El archivo de respaldo ya no existe en el disco.");
        }
    }
}