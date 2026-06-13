ALTER TABLE public.dry_clean_items RENAME TO items;
ALTER TABLE public.dry_clean_item_names RENAME TO item_names;
ALTER TABLE public.items RENAME CONSTRAINT dry_clean_items_organization_id_fkey TO fk_items_orangization_id;

ALTER TABLE public.items ADD COLUMN item_type character varying(255);
UPDATE public.items SET item_type = 'DRY_CLEANING';
ALTER TABLE public.items ALTER COLUMN item_type SET NOT NULL;
ALTER TABLE public.items ADD CONSTRAINT items_item_type_check CHECK (((item_type)::text = ANY ((ARRAY['WASH_AND_FOLD'::character varying, 'DRY_CLEANING'::character varying, 'OTHER'::character varying])::text[])));
