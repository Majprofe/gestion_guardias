// Servicio para la API de Supabase - Gestión de Coberturas
import { supabase } from '@/supabaseClient'

export const coberturasAPI = {
  // Obtener todas las coberturas
  async getCoberturas() {
    const { data, error } = await supabase
      .from('coberturas')
      .select(`
        *,
        ausencias (
          id,
          profesor_ausente_email,
          fecha,
          grupo,
          aula,
          hora,
          tarea
        )
      `)
      .order('created_at', { ascending: false })

    if (error) {
      console.error('Error obteniendo coberturas:', error)
      throw error
    }

    return data
  },

  // Crear nueva cobertura
  async crearCobertura(coberturaData) {
    const { data, error } = await supabase
      .from('coberturas')
      .insert([coberturaData])
      .select()

    if (error) {
      console.error('Error creando cobertura:', error)
      throw error
    }

    return data[0]
  },

  // Obtener cobertura por ausencia
  async getCoberturaPorAusencia(ausenciaId) {
    const { data, error } = await supabase
      .from('coberturas')
      .select('*')
      .eq('ausencia_id', ausenciaId)
      .single()

    if (error && error.code !== 'PGRST116') { // No encontrado
      console.error('Error obteniendo cobertura:', error)
      throw error
    }

    return data
  },

  // Obtener coberturas de un profesor
  async getCoberturasPorProfesor(profesorEmail) {
    const { data, error } = await supabase
      .from('coberturas')
      .select(`
        *,
        ausencias (
          id,
          profesor_ausente_email,
          fecha,
          grupo,
          aula,
          hora,
          tarea
        )
      `)
      .eq('profesor_cubre_email', profesorEmail)
      .order('created_at', { ascending: false })

    if (error) {
      console.error('Error obteniendo coberturas del profesor:', error)
      throw error
    }

    return data
  },

  // Eliminar cobertura
  async eliminarCobertura(id) {
    const { error } = await supabase
      .from('coberturas')
      .delete()
      .eq('id', id)

    if (error) {
      console.error('Error eliminando cobertura:', error)
      throw error
    }

    return true
  },

  // Asignar cobertura manual
  async asignarCobertura(ausenciaId, profesorEmail) {
    // Primero obtener datos de la ausencia
    const { data: ausencia, error: ausenciaError } = await supabase
      .from('ausencias')
      .select('*')
      .eq('id', ausenciaId)
      .single()

    if (ausenciaError) {
      console.error('Error obteniendo ausencia:', ausenciaError)
      throw ausenciaError
    }

    // Crear la cobertura
    const coberturaData = {
      ausencia_id: ausenciaId,
      profesor_cubre_email: profesorEmail,
      grupo: ausencia.grupo,
      aula: ausencia.aula
    }

    return this.crearCobertura(coberturaData)
  }
}
