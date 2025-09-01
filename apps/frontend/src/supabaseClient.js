
// Cliente de autenticación - Solo Supabase Auth (no base de datos)
import { createClient } from "@supabase/supabase-js";

const supabaseUrl = "https://eovgudiopsixoolebgvi.supabase.co";
const supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVvdmd1ZGlvcHNpeG9vbGViZ3ZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTU1Mzk5NDcsImV4cCI6MjA3MTExNTk0N30.MrbeQb7jDMyYjAuXEY4h5ugrBqhF2y4Ii0DCyW1YAAA";

// Usar Supabase solo para autenticación, no para base de datos
export const supabase = createClient(supabaseUrl, supabaseKey);