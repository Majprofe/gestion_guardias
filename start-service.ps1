# Script para cargar variables de entorno y ejecutar el servicio Spring Boot

# Cargar variables de entorno desde .env
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        $name = $matches[1]
        $value = $matches[2]
        Set-Item -Path "env:$name" -Value $value
        Write-Host "Establecida variable: $name"
    }
}

# Mostrar las variables cargadas para verificar
Write-Host "Variables cargadas:"
Write-Host "DATABASE_URL: $env:DATABASE_URL"
Write-Host "DATABASE_USERNAME: $env:DATABASE_USERNAME"
Write-Host "DATABASE_PASSWORD: ****"

# Cambiar al directorio del servicio
Set-Location apps\horarios

# Ejecutar el servicio
.\mvnw.cmd spring-boot:run
