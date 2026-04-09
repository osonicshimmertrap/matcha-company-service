-- Matcha Company Service — initial schema
-- V1__init.sql

-- ── Companies ──────────────────────────────────────────────────────────────

CREATE TABLE companies (
    id                    UUID         PRIMARY KEY,
    name                  VARCHAR(200) NOT NULL,
    description           TEXT,
    industry              VARCHAR(100),
    size_range            VARCHAR(20)  NOT NULL,
    website               VARCHAR(500),
    logo_url              VARCHAR(500),
    country               VARCHAR(100),
    city                  VARCHAR(100),
    founded_year          SMALLINT,
    head_approves_postings BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by_user_id    UUID         NOT NULL,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_companies_created_by ON companies (created_by_user_id);
CREATE INDEX idx_companies_industry   ON companies (industry);

-- ── Company members ────────────────────────────────────────────────────────

CREATE TABLE company_members (
    id         UUID        PRIMARY KEY,
    company_id UUID        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL,
    role       VARCHAR(20) NOT NULL,
    joined_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (company_id, user_id)
);

CREATE INDEX idx_company_members_company ON company_members (company_id);
CREATE INDEX idx_company_members_user    ON company_members (user_id);

-- ── HR invitation tokens ───────────────────────────────────────────────────

CREATE TABLE hr_invitation_tokens (
    id                UUID        PRIMARY KEY,
    company_id        UUID        NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    token             VARCHAR(64) NOT NULL UNIQUE,
    invited_by_user_id UUID       NOT NULL,
    invitee_email     VARCHAR(254),
    role              VARCHAR(20) NOT NULL DEFAULT 'HR',
    used              BOOLEAN     NOT NULL DEFAULT FALSE,
    expires_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_hr_invitation_tokens_company ON hr_invitation_tokens (company_id);
CREATE INDEX idx_hr_invitation_tokens_token   ON hr_invitation_tokens (token);
