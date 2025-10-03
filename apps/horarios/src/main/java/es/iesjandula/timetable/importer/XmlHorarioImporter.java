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
        Map<Integer, TramoHorario> tramos = new HashMap<>();
        NodeList tramoList = doc.getElementsByTagName("TRAMO");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        Map<Integer, Integer> contadorHoraPorDia = new HashMap<>();

        for (int i = 0; i < tramoList.getLength(); i++) {
            Element el = (Element) tramoList.item(i);
            Long id = parseLongSafe(el.getAttribute("num_tr"));
            Integer dia = parseIntSafe(el.getAttribute("numero_dia"));

            if (id == null || dia == null) continue;

            LocalTime ini = LocalTime.parse(el.getAttribute("hora_inicio").trim(), formatter);
            LocalTime fin = LocalTime.parse(el.getAttribute("hora_final").trim(), formatter);

            if (ini.equals(LocalTime.of(11, 0)) && fin.equals(LocalTime.of(11, 30))) continue;

            int nuevaHoraDia = contadorHoraPorDia.getOrDefault(dia, 0) + 1;
            contadorHoraPorDia.put(dia, nuevaHoraDia);

            TramoHorario tramo = new TramoHorario(id, dia, nuevaHoraDia, ini, fin);
            tramoHorarioService.save(tramo);
            tramos.put(id.intValue(), tramo);
        }

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

                TramoHorario tramo = tramoHorarioService.findById(tramoId);
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
