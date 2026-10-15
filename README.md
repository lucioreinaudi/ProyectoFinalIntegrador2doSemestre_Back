# ProyectoFinalIntegrador2doSemestre_Back
Repositorio oficial destinado al Trabajo Final Integrador de 2do Año (Solo Back)

## Alta del usuario municipio

Solo hay un usuario con rol `municipio` en todo el sistema (es el administrador).
No se crea desde el registro público (`POST /api/usuarios` siempre crea usuarios
`vecino`), se da de alta directo por SQL en Supabase.

### 1. Generar el hash de la contraseña

La contraseña no se guarda en texto plano, hay que hashearla con BCrypt antes de
insertarla. Hay un script que lo hace, usando las mismas dependencias que ya tiene
el proyecto (no hace falta instalar nada nuevo):

```powershell
.\scripts\generar-hash.ps1
```

Te va a pedir la contraseña por consola (elegila vos, no se guarda en ningún
archivo) y va a imprimir el hash. Copialo.

Requisito: haber corrido `mvnw.cmd compile` al menos una vez en este proyecto
(para que las dependencias ya estén descargadas en tu `~/.m2` local), y tener un
JDK con `javac` disponible (el JBR que trae IntelliJ sirve).

### 2. Insertar el usuario en Supabase

En el SQL Editor de Supabase:

```sql
INSERT INTO "Usuario" (email, nombre, password_hash, rol)
VALUES ('municipio@laboulaye.gob.ar', 'Municipalidad de Laboulaye', '<PEGAR_EL_HASH_ACA>', 'municipio');
```

Ajustá el email y el nombre según corresponda. El campo `rol` tiene que ser
exactamente `municipio` (en minúscula), es lo que exige el CHECK de la tabla.

### 3. Confirmar que funciona

Logueate con `POST /api/auth/login` usando ese email y la contraseña (la de
texto plano, no el hash). Si decodificás el token, el claim `rol` tiene que
decir `"municipio"`.