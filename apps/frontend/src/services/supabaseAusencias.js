// Servicio para la API de Supabase - Gestión de Ausencias
import { supabase } from '@/supabaseClient'

export const ausenciasAPI = {
  // Obtener todas las ausencias (con filtros opcionales)
  async getAusencias(fecha = null, profesorEmail = null) {
    let query = supabase
      .from('ausencias')
      .select(`
        *,
        coberturas (
          id,
          profesor_cubre_email,
          grupo,
          aula
        )
      `)
      .order('fecha', { ascending: false })
      .order('hora', { ascending: true })

    if (fecha) {
      query = query.eq('fecha', fecha)
    }

    if (profesorEmail) {
      query = query.eq('profesor_ausente_email', profesorEmail)
    }

    const { data, error } = await query

    if (error) {
      console.error('Error obteniendo ausencias:', error)
      throw error
    }

    return data
  },

  // Crear nueva ausencia
  async crearAusencia(ausenciaData) {
    const { data, error } = await supabase
      .from('ausencias')
      .insert([ausenciaData])
      .select()

    if (error) {
      console.error('Error creando ausencia:', error)
      throw error
    }

    return data[0]
  },

  // Actualizar ausencia
  async actualizarAusencia(id, ausenciaData) {
    const { data, error } = await supabase
      .from('ausencias')
      .update(ausenciaData)
      .eq('id', id)
      .select()

    if (error) {
      console.error('Error actualizando ausencia:', error)
      throw error
    }

    return data[0]
  },

  // Eliminar ausencia
  async eliminarAusencia(id) {
    const { error } = await supabase
      .from('ausencias')
      .delete()
      .eq('id', id)

    if (error) {
      console.error('Error eliminando ausencia:', error)
      throw error
    }

    return true
  },

  // Obtener ausencias por fecha específica
  async getAusenciasPorFecha(fecha) {
    return this.getAusencias(fecha)
  },

  // Obtener ausencias de un profesor
  async getAusenciasProfesor(profesorEmail) {
    return this.getAusencias(null, profesorEmail)
  }
}
