// XmlHorarioImporter.java
package es.iesjandula.timetable.importer;

import es.iesjandula.timetable.model.*;
import es.iesjandula.timetable.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class XmlHorarioImporter {

    private static final Logger log = LoggerFactory.getLogger(XmlHorarioImporter.class);

    private final ProfesorService profesorService;
    private final AsignaturaService asignaturaService;
    private final GrupoService grupoService;
    private final AulaService aulaService;
    private final TramoHorarioService tramoHorarioService;
    private final ActividadService actividadService;

    public XmlHorarioImporter(ProfesorService profesorService, AsignaturaService asignaturaService,
                              GrupoService grupoService, AulaService aulaService,
                              TramoHorarioService tramoHorarioService, ActividadService actividadService) {
        this.profesorService = profesorService;
        this.asignaturaService = asignaturaService;
        this.grupoService = grupoService;
        this.aulaService = aulaService;
        this.tramoHorarioService = tramoHorarioService;
        this.actividadService = actividadService;
    }

    private Long parseLongSafe(String value) {
        return (value != null && !value.trim().isEmpty()) ? Long.parseLong(value.trim()) : null;
    }

    private Integer parseIntSafe(String value) {
        return (value != null && !value.trim().isEmpty()) ? Integer.parseInt(value.trim()) : null;
    }

    public void importar(MultipartFile file) throws Exception {
        InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
        InputSource is = new InputSource(reader);
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(is);

        doc.getDocumentElement().normalize();

        // Desvincular relaciones entre Profesor y Grupo antes de borrar
        List<Profesor> profesoresAntesDeBorrar = profesorService.findAll();
        for (Profesor profesor : profesoresAntesDeBorrar) {
            profesor.setTutoria(null);
            profesorService.save(profesor);
        }

        // Limpieza
        actividadService.deleteAll();
        tramoHorarioService.deleteAll();
        aulaService.deleteAll();
        grupoService.deleteAll();
        asignaturaService.deleteAll();
        profesorService.deleteAll();

        // AULAS
        NodeList aulas = doc.getElementsByTagName("AULA");
        for (int i = 0; i < aulas.getLength(); i++) {
            Element aulaEl = (Element) aulas.item(i);
            Long id = parseLongSafe(aulaEl.getAttribute("num_int_au"));
            if (id == null) continue;
            Aula aula = new Aula(id, aulaEl.getAttribute("abreviatura").trim(), aulaEl.getAttribute("nombre").trim());
            aulaService.save(aula);
        }

        // GRUPOS
        NodeList grupos = doc.getElementsByTagName("GRUPO");
        for (int i = 0; i < grupos.getLength(); i++) {
            Element grupoEl = (Element) grupos.item(i);
            Long id = parseLongSafe(grupoEl.getAttribute("num_int_gr"));
            if (id == null) continue;

            String abreviatura = grupoEl.getAttribute("abreviatura").trim();
            String nombre = grupoEl.getAttribute("nombre").trim();
            boolean esProblematico = Boolean.parseBoolean(grupoEl.getAttribute("problematico")); // ← aquí

            Grupo grupo = new Grupo(id, abreviatura, nombre, esProblematico, null);
            grupoService.save(grupo);
        }


        // ASIGNATURAS
        NodeList asignaturas = doc.getElementsByTagName("ASIGNATURA");
        for (int i = 0; i < asignaturas.getLength(); i++) {
            Element asigEl = (Element) asignaturas.item(i);
            Long id = parseLongSafe(asigEl.getAttribute("num_int_as"));
            if (id == null) continue;
            Asignatura asignatura = new Asignatura(id,
                    asigEl.getAttribute("abreviatura").trim(),
                    asigEl.getAttribute("nombre").trim(),
                    asigEl.getAttribute("nivel").trim(),
                    asigEl.getAttribute("curso").trim());
            asignaturaService.save(asignatura);
        }

        // PROFESORES (sin tutoría)
        NodeList profesores = doc.getElementsByTagName("PROFESOR");
        for (int i = 0; i < profesores.getLength(); i++) {
            Element profEl = (Element) profesores.item(i);
            Long id = parseLongSafe(profEl.getAttribute("num_int_pr"));
            if (id == null || id == 0) continue;

            String abreviatura = profEl.getAttribute("abreviatura").trim();
            String nombre = profEl.getAttribute("nombre").trim();
            String departamento = profEl.getAttribute("nivel").trim();
            String email = profEl.getAttribute("email").trim();


            Profesor profesor = new Profesor(id, nombre, abreviatura, departamento, email, 0, 0, 0, null);
            profesorService.save(profesor);
        }

        // Tutoría
        for (int i = 0; i < profesores.getLength(); i++) {
            Element el = (Element) profesores.item(i);
            Long idProf = parseLongSafe(el.getAttribute("num_int_pr"));
            Long grupoId = parseLongSafe(el.getAttribute("num_gr_tutoria_principal"));
            if (idProf != null && grupoId != null) {
                Profesor profesor = profesorService.findById(idProf);
                Grupo grupoTutoria = grupoService.findById(grupoId);
                if (profesor != null && grupoTutoria != null) {
                    profesor.setTutoria(grupoTutoria);
                    profesorService.save(profesor);
                }
            }
        }

        // TRAMOS HORARIOS (renumerados sin recreo)
        // Solo cargamos horas lectivas (6 por día), excluyendo el recreo
        Map<Long, TramoHorario> tramosMap = new HashMap<>(); // Mapeo ID_XML -> TramoHorario guardado
        NodeList tramoList = doc.getElementsByTagName("TRAMO");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        Map<Integer, Integer> contadorHoraPorDia = new HashMap<>();
        
        long nuevoId = 1; // ID secuencial para los tramos (1-30)

        for (int i = 0; i < tramoList.getLength(); i++) {
            Element el = (Element) tramoList.item(i);
            Long idOriginalXml = parseLongSafe(el.getAttribute("num_tr"));
            Integer dia = parseIntSafe(el.getAttribute("numero_dia"));

            if (idOriginalXml == null || dia == null) continue;

            LocalTime ini = LocalTime.parse(el.getAttribute("hora_inicio").trim(), formatter);
            LocalTime fin = LocalTime.parse(el.getAttribute("hora_final").trim(), formatter);

            // EXCLUIR RECREO: Detectar tramos de recreo (30 minutos entre las 11:00 y 11:45)
            // Casos comunes:
            //   - 11:00-11:30 (algunos institutos)
            //   - 11:15-11:45 (nuestro instituto actual)
            // Criterio: tramo de 30 minutos que empiece entre 11:00 y 11:15
            boolean esRecreo = (ini.getHour() == 11 && ini.getMinute() >= 0 && ini.getMinute() <= 15) &&
                               (fin.getHour() == 11 && fin.getMinute() >= 30 && fin.getMinute() <= 45) &&
                               (java.time.Duration.between(ini, fin).toMinutes() == 30);
            
            if (esRecreo) {
                log.info("⏭️  Omitiendo recreo: Día {} - Tramo XML {} ({} - {})", dia, idOriginalXml, ini, fin);
                continue;
            }

            // Renumerar hora del día (1-6 en lugar de 1-7)
            int nuevaHoraDia = contadorHoraPorDia.getOrDefault(dia, 0) + 1;
            contadorHoraPorDia.put(dia, nuevaHoraDia);

            // IMPORTANTE: Usar nuevoId secuencial (1-30) en lugar del ID original del XML
            TramoHorario tramo = new TramoHorario(nuevoId, dia, nuevaHoraDia, ini, fin);
            tramoHorarioService.save(tramo);
            
            // Guardar mapeo: ID_XML -> TramoHorario (para usar al cargar actividades)
            tramosMap.put(idOriginalXml, tramo);
            
            log.debug("✅ Tramo guardado: ID {} (XML:{}) - Día {} Hora {} ({} - {})", 
                     nuevoId, idOriginalXml, dia, nuevaHoraDia, ini, fin);
            
            nuevoId++; // Incrementar ID secuencial
        }
        
        log.info("📊 Total tramos horarios cargados: {} (debe ser 30 = 6 horas x 5 días)", tramosMap.size());

        // HORARIO PROFESORES
        NodeList horariosProfesores = doc.getElementsByTagName("HORARIO_PROF");
        for (int i = 0; i < horariosProfesores.getLength(); i++) {
            Element horarioProfEl = (Element) horariosProfesores.item(i);
            Long idProfesor = parseLongSafe(horarioProfEl.getAttribute("hor_num_int_pr"));
            if (idProfesor == null) continue;

            Profesor profesor = profesorService.findById(idProfesor);
            if (profesor == null) continue;

            NodeList actividades = horarioProfEl.getElementsByTagName("ACTIVIDAD");
            for (int j = 0; j < actividades.getLength(); j++) {
                Element actividadEl = (Element) actividades.item(j);

                Long tramoId = parseLongSafe(actividadEl.getAttribute("tramo"));
                Long asignaturaId = parseLongSafe(actividadEl.getAttribute("asignatura"));
                Long aulaId = parseLongSafe(actividadEl.getAttribute("aula"));

                if (tramoId == null || asignaturaId == null || aulaId == null) continue;

                // Buscar el tramo usando el mapeo ID_XML -> TramoHorario
                // Si el tramo fue excluido (recreo), no estará en el mapa
                TramoHorario tramo = tramosMap.get(tramoId);
                if (tramo == null) {
                    // Tramo no existe (fue excluido como recreo)
                    log.debug("⏭️  Omitiendo actividad para tramo XML {} (recreo excluido)", tramoId);
                    continue;
                }
                
                Asignatura asignatura = asignaturaService.findById(asignaturaId);
                Aula aula = aulaService.findById(aulaId);

                if (tramo == null || asignatura == null || aula == null) continue;

                TipoActividad tipo;
                String abrev = asignatura.getAbreviatura().toUpperCase();
                String nivel = asignatura.getNivel().toUpperCase();

                if (abrev.equals("GUAR")) {
                    tipo = TipoActividad.GUARDIA;
                } else if (abrev.contains("CT") || abrev.contains("COOR") || abrev.contains("REDUC") ||
                        abrev.contains("REUN") || abrev.contains("M55") || abrev.contains("RED") ||
                        abrev.contains("G30") || abrev.contains("ETCP") || abrev.contains("TOR") || nivel.contains("OTR")) {
                    tipo = TipoActividad.OTRA;
                } else {
                    tipo = TipoActividad.CLASE;
                }

                List<Grupo> gruposActividad = new ArrayList<>();
                Element gruposEl = (Element) actividadEl.getElementsByTagName("GRUPOS_ACTIVIDAD").item(0);
                if (gruposEl != null) {
                    int totalGrupos = parseIntSafe(gruposEl.getAttribute("tot_gr_act")) != null ? parseIntSafe(gruposEl.getAttribute("tot_gr_act")) : 0;
                    for (int g = 1; g <= totalGrupos; g++) {
                        String attr = "grupo_" + g;
                        Long grupoId = parseLongSafe(gruposEl.getAttribute(attr));
                        if (grupoId != null) {
                            Grupo grupo = grupoService.findById(grupoId);
                            if (grupo != null) gruposActividad.add(grupo);
                        }
                    }
                }

                Actividad actividad = new Actividad(null, profesor, tramo, asignatura, aula, tipo, gruposActividad);
                actividadService.save(actividad);
            }
        }

        log.info("✅ Importación completa desde XML");
    }
}
