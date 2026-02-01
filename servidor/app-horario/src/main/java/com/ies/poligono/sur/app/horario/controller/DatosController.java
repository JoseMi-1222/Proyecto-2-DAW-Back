package com.ies.poligono.sur.app.horario.controller;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ies.poligono.sur.app.horario.dto.BackupDataDTO;
import com.ies.poligono.sur.app.horario.dto.PostImportacionInputDTO;
import com.ies.poligono.sur.app.horario.processor.HorarioServiceProcessor;
import com.ies.poligono.sur.app.horario.service.BackupService;

@RestController
@RequestMapping("/api/datos")
public class DatosController {

    @Autowired
    private BackupService backupService;

    @Autowired
    private HorarioServiceProcessor horarioServiceProcessor;

    @PostMapping("/importar")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('administrador')")
    public ResponseEntity<String> importarArchivo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío.");
        }

        String nombre = file.getOriginalFilename();
        
        try {
            if (nombre != null && nombre.toLowerCase().endsWith(".json")) {
                backupService.procesarImportacionJson(file);
                return ResponseEntity.ok("Copia de seguridad (JSON) restaurada correctamente. Base de datos actualizada.");
            } 
            
            else if (nombre != null && nombre.toLowerCase().endsWith(".txt")) {
                byte[] fileContent = file.getBytes();
                String encodedString = Base64.getEncoder().encodeToString(fileContent);

                PostImportacionInputDTO dto = new PostImportacionInputDTO();
                dto.setFile(encodedString);

                horarioServiceProcessor.importarHorario(dto);
                
                return ResponseEntity.ok("Horario inicial (TXT) cargado y procesado correctamente.");
            } 
            
            else {
                return ResponseEntity.badRequest().body("Formato no soportado. Por favor sube un .json o un .txt");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error crítico al procesar el archivo: " + e.getMessage());
        }
    }


    @GetMapping("/exportar/json")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('administrador')")
    public ResponseEntity<BackupDataDTO> descargarJson() {
        return ResponseEntity.ok(backupService.exportarDatosCompletos());
    }

    @GetMapping("/exportar/txt")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('administrador')")
    public ResponseEntity<String> descargarTxt() {
        String contenidoTxt = backupService.generarTxtHorario();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"horario_instituto.txt\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .body(contenidoTxt);
    }
}