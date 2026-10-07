-- Script de creación de tablas para PostgreSQL
-- Basado en las entidades del Backend de Easy Balance (mybalance-v2)

-- 1. Tabla de Cuentas (Account)
CREATE TABLE IF NOT EXISTS account (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    balance NUMERIC(38, 2) NOT NULL
);

-- 2. Tabla de Configuración de Presupuestos (BudgetConfig)
CREATE TABLE IF NOT EXISTS budget_config (
    id UUID PRIMARY KEY,
    total_amount NUMERIC(38, 2) NOT NULL,
    vida_percentage INTEGER NOT NULL,
    ocio_percentage INTEGER NOT NULL,
    inversion_percentage INTEGER NOT NULL
);

-- 3. Tabla de Etiquetas (Tag)
CREATE TABLE IF NOT EXISTS tag (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    icon VARCHAR(255) NOT NULL DEFAULT 'HelpCircle'
);

-- 4. Tabla de Gastos Fijos Maestros (FixedExpense)
CREATE TABLE IF NOT EXISTS fixed_expense (
    id UUID PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    category VARCHAR(255) NOT NULL, -- Valores de Enum: VIDA, OCIO, INVERSION
    due_day INTEGER NOT NULL,
    tag_id UUID REFERENCES tag(id) ON DELETE SET NULL
);

-- 5. Tabla de Gastos Variables / Registros de Gastos (Expense)
CREATE TABLE IF NOT EXISTS expense (
    id UUID PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    category VARCHAR(255) NOT NULL, -- Valores de Enum: VIDA, OCIO, INVERSION
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    account_id UUID,
    fixed_expense_id UUID,
    tag_id UUID REFERENCES tag(id) ON DELETE SET NULL
);

-- 6. Tabla de Historial Mensual de Gastos Fijos (MonthlyFixedExpense)
CREATE TABLE IF NOT EXISTS monthly_fixed_expense (
    id UUID PRIMARY KEY,
    year_month VARCHAR(255) NOT NULL, -- Ej: "2026-08"
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    category VARCHAR(255) NOT NULL, -- Valores de Enum: VIDA, OCIO, INVERSION
    due_day INTEGER NOT NULL,
    tag_id UUID REFERENCES tag(id) ON DELETE SET NULL
);

-- 7. Tabla de Usuarios (users)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- 8. Tabla de Configuración Mensual de Presupuesto (MonthlyBudgetConfig)
CREATE TABLE IF NOT EXISTS monthly_budget_config (
    id UUID PRIMARY KEY,
    year_month VARCHAR(255) NOT NULL UNIQUE,
    total_amount NUMERIC(38, 2) NOT NULL,
    vida_percentage INTEGER NOT NULL,
    ocio_percentage INTEGER NOT NULL,
    inversion_percentage INTEGER NOT NULL
);

