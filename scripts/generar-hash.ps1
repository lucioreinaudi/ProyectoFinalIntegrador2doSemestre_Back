<#
.SYNOPSIS
    Genera el hash BCrypt de una contrasena, para dar de alta el usuario
    municipio (o cualquier otro) directo por SQL en Supabase.
.DESCRIPTION
    Te pide la contrasena en el momento (no se escribe en este archivo ni
    se le pasa a nadie mas que a tu propia PC). Copia el hash que imprime
    al final y pegalo en el INSERT de la seccion "Alta del usuario
    municipio" del README.
.EXAMPLE
    .\generar-hash.ps1
#>

$jbrHome = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr"
if (-not (Test-Path $jbrHome)) {
    $jbrHome = "C:\Program Files\JetBrains\IntelliJ IDEA*\jbr" | Resolve-Path -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Path
}
if (-not $jbrHome -or -not (Test-Path "$jbrHome\bin\javac.exe")) {
    Write-Error "No encontre un JDK con javac (ni en JAVA_HOME ni en el JBR de IntelliJ). Instala un JDK 17+ o corre esto desde una terminal que ya tenga 'javac' en el PATH."
    exit 1
}

$jarCrypto = Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\security\spring-security-crypto" -Recurse -Filter "spring-security-crypto-*.jar" -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1 -ExpandProperty FullName
$jarLogging = Get-ChildItem "$env:USERPROFILE\.m2\repository\commons-logging" -Recurse -Filter "commons-logging-*.jar" -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1 -ExpandProperty FullName

if (-not $jarCrypto -or -not $jarLogging) {
    Write-Error "No encontre las dependencias en tu repositorio local de Maven (~/.m2). Corre 'mvnw.cmd compile' al menos una vez en este proyecto para que se descarguen, y volve a intentar."
    exit 1
}

$dir = Join-Path $env:TEMP "generar-hash-bcrypt"
New-Item -ItemType Directory -Force -Path $dir | Out-Null
$codigo = @'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        System.out.println(enc.encode(args[0]));
    }
}
'@
[System.IO.File]::WriteAllText((Join-Path $dir "GenerarHash.java"), $codigo)

Push-Location $dir
& "$jbrHome\bin\javac.exe" -cp "$jarCrypto" GenerarHash.java
if ($LASTEXITCODE -ne 0) { Pop-Location; exit 1 }

$contraseniaPlana = Read-Host "Contraseña para el usuario municipio"

Write-Host ""
Write-Host "Hash generado (copialo entero en el INSERT del README):"
& "$jbrHome\bin\java.exe" -cp ".;$jarCrypto;$jarLogging" GenerarHash "$contraseniaPlana"

Pop-Location
Remove-Item $dir -Recurse -Force
