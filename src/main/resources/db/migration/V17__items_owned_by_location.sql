ALTER TABLE public.items DROP CONSTRAINT fk_items_orangization_id;
DROP INDEX idx_dry_clean_items_organization_id;
ALTER TABLE public.items DROP COLUMN organization_id;

ALTER TABLE public.items ADD COLUMN location_id uuid NOT NULL REFERENCES public.locations(id);
CREATE INDEX idx_items_location_id ON public.items (location_id);
