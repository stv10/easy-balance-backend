-- Migración: Crear tabla tag y agregar tag_id a expense, fixed_expense y monthly_fixed_expense

CREATE TABLE IF NOT EXISTS tag (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    icon VARCHAR(255) NOT NULL DEFAULT 'HelpCircle'
);

ALTER TABLE expense ADD COLUMN IF NOT EXISTS tag_id UUID REFERENCES tag(id) ON DELETE SET NULL;
ALTER TABLE fixed_expense ADD COLUMN IF NOT EXISTS tag_id UUID REFERENCES tag(id) ON DELETE SET NULL;
ALTER TABLE monthly_fixed_expense ADD COLUMN IF NOT EXISTS tag_id UUID REFERENCES tag(id) ON DELETE SET NULL;
