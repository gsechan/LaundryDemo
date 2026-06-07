CREATE TABLE public.dry_clean_items (
    id uuid NOT NULL PRIMARY KEY,
    organization_id uuid NOT NULL REFERENCES public.organizations(id),
    price numeric(10,2) NOT NULL
);

CREATE INDEX idx_dry_clean_items_organization_id ON public.dry_clean_items (organization_id);

CREATE TABLE public.dry_clean_item_names (
    id uuid NOT NULL PRIMARY KEY,
    item_id uuid NOT NULL REFERENCES public.dry_clean_items(id),
    locale character varying(255) NOT NULL,
    name character varying(255) NOT NULL
);

CREATE INDEX idx_dry_clean_item_names_item_id ON public.dry_clean_item_names (item_id);
