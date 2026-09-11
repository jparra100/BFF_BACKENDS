param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("web", "atm", "mobile")]
    [string]$Servicio
)

$variablesPassword = @{
    web = "WEB_SSL_KEYSTORE_PASSWORD"
    atm = "ATM_SSL_KEYSTORE_PASSWORD"
    mobile = "MOBILE_SSL_KEYSTORE_PASSWORD"
}

$nombreVariable = $variablesPassword[$Servicio]
$password = [Environment]::GetEnvironmentVariable($nombreVariable)

if ([string]::IsNullOrWhiteSpace($password)) {
    throw "Debe definir la variable de entorno $nombreVariable antes de generar el certificado."
}

$raizProyecto = Split-Path -Parent $PSScriptRoot
$directorioCertificados = Join-Path $raizProyecto "bff-$Servicio\certs"
$archivoCertificado = Join-Path $directorioCertificados "bff-$Servicio.p12"
$alias = "bff-$Servicio"

New-Item -ItemType Directory -Force -Path $directorioCertificados | Out-Null

$keytool = if ($env:JAVA_HOME) {
    Join-Path $env:JAVA_HOME "bin\keytool.exe"
} else {
    (Get-Command keytool -ErrorAction Stop).Source
}

if (-not (Test-Path -LiteralPath $keytool)) {
    throw "No se encontró keytool. Configure JAVA_HOME con una instalación de Java 17."
}

& $keytool `
    -genkeypair `
    -alias $alias `
    -keyalg RSA `
    -keysize 2048 `
    -validity 3650 `
    -storetype PKCS12 `
    -keystore $archivoCertificado `
    -storepass $password `
    -keypass $password `
    -dname "CN=localhost, OU=Desarrollo, O=Banco XYZ, L=Santiago, ST=RM, C=CL" `
    -ext "SAN=dns:localhost,ip:127.0.0.1"

if ($LASTEXITCODE -ne 0) {
    throw "No fue posible generar el certificado para $Servicio."
}

Write-Output "Certificado generado en $archivoCertificado"
