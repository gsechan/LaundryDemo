-- Organizations are soft-deleted: deletion sets is_deleted = true rather than
-- removing the row (which would orphan/destroy tenant data). A soft-deleted
-- org's users can no longer log in.

ALTER TABLE public.organizations
    ADD COLUMN is_deleted boolean NOT NULL DEFAULT false;
