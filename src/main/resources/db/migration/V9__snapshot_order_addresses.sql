-- Orders now snapshot the pickup/dropoff address fields instead of referencing
-- the address by id, so an order keeps its historical address even if the
-- underlying address is later changed or deleted.

-- Dev only: clear existing orders so the new NOT NULL snapshot columns can be
-- added without backfilling. order_lines first, since it references orders.
DELETE FROM public.order_lines;
DELETE FROM public.orders;

ALTER TABLE public.orders DROP CONSTRAINT IF EXISTS orders_pickup_address_id_fkey;
ALTER TABLE public.orders DROP CONSTRAINT IF EXISTS orders_dropoff_address_id_fkey;

ALTER TABLE public.orders DROP COLUMN pickup_address_id;
ALTER TABLE public.orders DROP COLUMN dropoff_address_id;

ALTER TABLE public.orders
    ADD COLUMN pickup_street1 character varying(255) NOT NULL,
    ADD COLUMN pickup_street2 character varying(255),
    ADD COLUMN pickup_city character varying(255) NOT NULL,
    ADD COLUMN pickup_state character varying(255) NOT NULL,
    ADD COLUMN pickup_country character varying(255) NOT NULL,
    ADD COLUMN pickup_postcode character varying(255) NOT NULL,
    ADD COLUMN dropoff_street1 character varying(255) NOT NULL,
    ADD COLUMN dropoff_street2 character varying(255),
    ADD COLUMN dropoff_city character varying(255) NOT NULL,
    ADD COLUMN dropoff_state character varying(255) NOT NULL,
    ADD COLUMN dropoff_country character varying(255) NOT NULL,
    ADD COLUMN dropoff_postcode character varying(255) NOT NULL;
