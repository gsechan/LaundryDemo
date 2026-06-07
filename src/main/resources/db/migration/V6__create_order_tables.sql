CREATE TABLE public.orders (
                               id uuid NOT NULL PRIMARY KEY,
                               completed timestamp(6) without time zone,
                               last_change timestamp(6) without time zone NOT NULL,
                               scheduled_dropoff timestamp(6) without time zone NOT NULL,
                               scheduled_pickup timestamp(6) without time zone NOT NULL,
                               state character varying(255) NOT NULL,
                               submitted timestamp(6) without time zone NOT NULL,
                               user_id uuid NOT NULL REFERENCES public.users(id),
                               dropoff_address_id uuid NOT NULL REFERENCES public.addresses(id),
                               pickup_address_id uuid NOT NULL REFERENCES public.addresses(id),
                               CONSTRAINT orders_state_check CHECK (((state)::text = ANY ((ARRAY['SUBMITTED'::character varying, 'PICKUP_IN_PROGRESS'::character varying, 'PICKED_UP'::character varying, 'CLEANING'::character varying, 'AWAITING_DROP_OFF'::character varying, 'DROPPING_OFF'::character varying, 'COMPLETED'::character varying])::text[])))
);
CREATE INDEX idx_orders_user_id ON public.orders (user_id);


CREATE TABLE public.order_lines (
                                    id uuid NOT NULL PRIMARY KEY,
                                    item_type character varying(255) NOT NULL,
                                    name_in_english_locale character varying(255),
                                    name_in_org_locale character varying(255),
                                    name_in_submitted_locale character varying(255),
                                    org_locale character varying(255) NOT NULL,
                                    price_per_unit numeric(38,2) NOT NULL,
                                    quantity numeric(38,2),
                                    submitted_locale character varying(255) NOT NULL,
                                    total_cost numeric(38,2),
                                    order_id uuid REFERENCES public.orders(id),
                                    CONSTRAINT order_line_item_type_check CHECK (((item_type)::text = ANY ((ARRAY['WASH_AND_FOLD'::character varying, 'DRY_CLEANING'::character varying, 'OTHER'::character varying])::text[])))
);
CREATE INDEX idx_order_lines_order_id ON public.order_lines (order_id);
