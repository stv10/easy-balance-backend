-- Migración: Crear tabla tag y agregar tag_id a expense, fixed_expense y monthly_fixed_expense

CREATE TABLE IF NOT EXISTS tag (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    icon VARCHAR(255) NOT NULL DEFAULT 'HelpCircle',
    color VARCHAR(50) NOT NULL DEFAULT '#3b82f6'
);

ALTER TABLE tag ADD COLUMN IF NOT EXISTS color VARCHAR(50) DEFAULT '#3b82f6';
UPDATE tag SET color = '#3b82f6' WHERE color IS NULL;
ALTER TABLE expense ADD COLUMN IF NOT EXISTS tag_id UUID REFERENCES tag(id) ON DELETE SET NULL;
ALTER TABLE fixed_expense ADD COLUMN IF NOT EXISTS tag_id UUID REFERENCES tag(id) ON DELETE SET NULL;
ALTER TABLE monthly_fixed_expense ADD COLUMN IF NOT EXISTS tag_id UUID REFERENCES tag(id) ON DELETE SET NULL;
