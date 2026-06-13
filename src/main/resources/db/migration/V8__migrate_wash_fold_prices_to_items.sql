WITH inserted_items AS (
    INSERT INTO public.items (id, organization_id, price, item_type)
    SELECT gen_random_uuid(), organization_id, price, 'WASH_AND_FOLD'
    FROM public.wash_fold_prices
    RETURNING id
)
INSERT INTO public.item_names (id, item_id, name, locale)
SELECT gen_random_uuid(), inserted_items.id, 'Wash and fold', 'en-US'
FROM inserted_items;

ALTER TABLE public.wash_fold_prices DROP COLUMN avg_weight;

DROP TABLE public.wash_fold_prices;
