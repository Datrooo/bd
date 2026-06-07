-- =========================================================
-- GARAGE INTEGRITY RULES
-- =========================================================

CREATE OR REPLACE FUNCTION trg_garage_object_check_parent_cycle()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.parent_object_id IS NULL THEN
        RETURN NEW;
    END IF;

    IF NEW.id IS NOT NULL AND NEW.parent_object_id = NEW.id THEN
        RAISE EXCEPTION 'Объект гаражного хозяйства не может быть родителем самого себя';
    END IF;

    IF NEW.id IS NOT NULL AND EXISTS (
        WITH RECURSIVE ancestors(id, parent_object_id, path) AS (
            SELECT go.id, go.parent_object_id, ARRAY[go.id]
            FROM garage_object go
            WHERE go.id = NEW.parent_object_id

            UNION ALL

            SELECT parent.id, parent.parent_object_id, ancestors.path || parent.id
            FROM garage_object parent
            JOIN ancestors ON parent.id = ancestors.parent_object_id
            WHERE NOT parent.id = ANY(ancestors.path)
        )
        SELECT 1
        FROM ancestors
        WHERE ancestors.id = NEW.id
    ) THEN
        RAISE EXCEPTION 'Объект гаражного хозяйства не может ссылаться на своего потомка как на родителя';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_garage_object_check_parent_cycle
BEFORE INSERT OR UPDATE OF parent_object_id ON garage_object
FOR EACH ROW EXECUTE FUNCTION trg_garage_object_check_parent_cycle();

CREATE OR REPLACE FUNCTION fn_check_garage_object_capacity(p_garage_object_id BIGINT, p_capacity INT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    v_max_occupied INT;
BEGIN
    IF p_capacity IS NULL THEN
        RETURN;
    END IF;

    SELECT COALESCE(MAX(occupied_count), 0)
    INTO v_max_occupied
    FROM (
        SELECT COUNT(*)::INT AS occupied_count
        FROM vehicle_location_history location_point
        JOIN vehicle_location_history location_active
          ON location_active.garage_object_id = location_point.garage_object_id
         AND location_active.start_date <= location_point.start_date
         AND (location_active.end_date IS NULL OR location_active.end_date >= location_point.start_date)
        WHERE location_point.garage_object_id = p_garage_object_id
        GROUP BY location_point.id
    ) occupancy;

    IF v_max_occupied > p_capacity THEN
        RAISE EXCEPTION 'Вместимость объекта гаражного хозяйства id=% превышена: занято %, вместимость %',
            p_garage_object_id, v_max_occupied, p_capacity;
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION trg_garage_object_check_capacity()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    PERFORM fn_check_garage_object_capacity(NEW.id, NEW.capacity);
    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_garage_object_check_capacity
BEFORE UPDATE OF capacity ON garage_object
FOR EACH ROW EXECUTE FUNCTION trg_garage_object_check_capacity();
