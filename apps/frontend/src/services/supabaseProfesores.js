// Servicio para la API de Supabase - Gestión de Profesores
import { supabase } from '@/supabaseClient'

export const profesoresAPI = {
  // Obtener todos los profesores
  async getProfesores() {
    const { data, error } = await supabase
      .from('profesores')
      .select('*')
      .eq('activo', true)
      .order('nombre', { ascending: true })

    if (error) {
      console.error('Error obteniendo profesores:', error)
      throw error
    }

    return data
  },

  // Obtener profesor por email
  async getProfesorPorEmail(email) {
    const { data, error } = await supabase
      .from('profesores')
      .select('*')
      .eq('email', email)
      .eq('activo', true)
      .single()

    if (error && error.code !== 'PGRST116') { // No encontrado
      console.error('Error obteniendo profesor:', error)
      throw error
    }

    return data
  },

  // Crear o actualizar profesor
  async upsertProfesor(profesorData) {
    const { data, error } = await supabase
      .from('profesores')
      .upsert([profesorData], { onConflict: 'email' })
      .select()

    if (error) {
      console.error('Error guardando profesor:', error)
      throw error
    }

    return data[0]
  },

  // Obtener profesores con guardias (para asignación automática)
  async getProfesoresConGuardias(diaSemana, hora) {
    const { data, error } = await supabase
      .from('actividades')
      .select(`
        profesor_id,
        profesores (
          id,
          email,
          nombre,
          guardias_realizadas,
          guardias_problematicas
        )
      `)
      .eq('tipo', 'GUARDIA')
      .eq('tramos_horarios.dia_semana', diaSemana)
      .eq('tramos_horarios.hora_dia', hora)

    if (error) {
      console.error('Error obteniendo profesores de guardia:', error)
      throw error
    }

    return data.map(item => item.profesores)
  },

  // Incrementar guardias realizadas
  async incrementarGuardias(profesorId, esProblematica = false) {
    const campo = esProblematica ? 'guardias_problematicas' : 'guardias_realizadas'
    
    const { data, error } = await supabase.rpc('incrementar_guardias', {
      profesor_id: profesorId,
      campo_a_incrementar: campo
    })

    if (error) {
      console.error('Error incrementando guardias:', error)
      throw error
    }

    return data
  },

  // Verificar si el profesor es administrador
  async esAdmin(email) {
    const profesor = await this.getProfesorPorEmail(email)
    return profesor?.es_admin || false
  }
}
