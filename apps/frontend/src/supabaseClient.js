
// Cliente de autenticación - Solo Supabase Auth (no base de datos)
import { createClient } from "@supabase/supabase-js";

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
const supabaseKey = import.meta.env.VITE_SUPABASE_ANON_KEY;

// Usar Supabase solo para autenticación, no para base de datos
export const supabase = createClient(supabaseUrl, supabaseKey);