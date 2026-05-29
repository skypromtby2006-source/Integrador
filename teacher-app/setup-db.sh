#!/bin/bash
# setup-db.sh
# Crea la base de datos 'didactai' en PostgreSQL local.
# Ejecuta este script UNA VEZ antes de correr la app.
#
# Uso: bash setup-db.sh
# Si tu usuario de postgres no es 'postgres', cambia el -U

echo "→ Creando base de datos 'didactai'..."
psql -U postgres -c "CREATE DATABASE didactai;" 2>/dev/null \
  && echo "✓ Base de datos creada." \
  || echo "⚠ La base de datos ya existe (normal si ya corriste esto antes)."

echo ""
echo "→ Conexión configurada en:"
echo "   Host:     localhost:5432"
echo "   DB:       didactai"
echo "   Usuario:  postgres"
echo "   Password: postgres"
echo ""
echo "Si tu contraseña es diferente, edita:"
echo "   src/main/kotlin/db/DatabaseConfig.kt → línea PASSWORD"
echo ""
echo "Listo. Puedes correr la app con: ./gradlew run"

echo ""
echo "→ Creando tabla 'sesion_quiz' (si no existe)..."
psql -U postgres -d didactai -c "
CREATE TABLE IF NOT EXISTS sesion_quiz (
    sesion_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id    VARCHAR(20) NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    organ_id      VARCHAR(50) NOT NULL,
    correctas     SMALLINT NOT NULL DEFAULT 0,
    incorrectas   SMALLINT NOT NULL DEFAULT 0,
    saltadas      SMALLINT NOT NULL DEFAULT 0,
    puntaje_total FLOAT,
    realizado_en  TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_sesion_quiz_usuario ON sesion_quiz(usuario_id);
CREATE INDEX IF NOT EXISTS idx_sesion_quiz_organ   ON sesion_quiz(organ_id);
" && echo "✓ Tabla sesion_quiz OK." || echo "⚠ Error al crear tabla sesion_quiz."
