-- =========================================================
-- ФУНКЦИИ И ТРИГГЕРЫ
-- =========================================================

CREATE OR REPLACE FUNCTION fn_recalculate_vehicle_current_mileage(p_vehicle_id BIGINT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE vehicle v
    SET current_mileage = COALESCE((
        SELECT SUM(tr.mileage_km)
        FROM transportation_record tr
        WHERE tr.vehicle_id = p_vehicle_id
    ), 0)
    WHERE v.id = p_vehicle_id;
END;
$$;

CREATE OR REPLACE FUNCTION trg_transportation_record_recalculate_mileage()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        PERFORM fn_recalculate_vehicle_current_mileage(NEW.vehicle_id);
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.vehicle_id IS DISTINCT FROM NEW.vehicle_id THEN
            PERFORM fn_recalculate_vehicle_current_mileage(OLD.vehicle_id);
        END IF;
        PERFORM fn_recalculate_vehicle_current_mileage(NEW.vehicle_id);
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM fn_recalculate_vehicle_current_mileage(OLD.vehicle_id);
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER tr_transportation_record_recalculate_mileage
AFTER INSERT OR UPDATE OR DELETE ON transportation_record
FOR EACH ROW EXECUTE FUNCTION trg_transportation_record_recalculate_mileage();

CREATE OR REPLACE FUNCTION fn_recalculate_repair_total_cost(p_repair_id BIGINT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    v_total_work_cost NUMERIC(14,2);
    v_total_component_cost NUMERIC(14,2);
BEGIN
    SELECT COALESCE(SUM(rw.cost), 0)
    INTO v_total_work_cost
    FROM repair_work rw
    WHERE rw.repair_id = p_repair_id;

    SELECT COALESCE(SUM(vch.cost), 0)
    INTO v_total_component_cost
    FROM vehicle_component_history vch
    WHERE vch.repair_id = p_repair_id;

    UPDATE repair r
    SET total_cost = COALESCE(v_total_work_cost, 0) + COALESCE(v_total_component_cost, 0)
    WHERE r.id = p_repair_id;
END;
$$;

CREATE OR REPLACE FUNCTION trg_repair_work_recalculate_total_cost()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        PERFORM fn_recalculate_repair_total_cost(NEW.repair_id);
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.repair_id IS DISTINCT FROM NEW.repair_id THEN
            PERFORM fn_recalculate_repair_total_cost(OLD.repair_id);
        END IF;
        PERFORM fn_recalculate_repair_total_cost(NEW.repair_id);
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM fn_recalculate_repair_total_cost(OLD.repair_id);
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER tr_repair_work_recalculate_total_cost
AFTER INSERT OR UPDATE OR DELETE ON repair_work
FOR EACH ROW EXECUTE FUNCTION trg_repair_work_recalculate_total_cost();

CREATE OR REPLACE FUNCTION trg_vehicle_component_history_recalculate_total_cost()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.repair_id IS NOT NULL THEN
            PERFORM fn_recalculate_repair_total_cost(NEW.repair_id);
        END IF;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.repair_id IS NOT NULL AND OLD.repair_id IS DISTINCT FROM NEW.repair_id THEN
            PERFORM fn_recalculate_repair_total_cost(OLD.repair_id);
        END IF;
        IF NEW.repair_id IS NOT NULL THEN
            PERFORM fn_recalculate_repair_total_cost(NEW.repair_id);
        END IF;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        IF OLD.repair_id IS NOT NULL THEN
            PERFORM fn_recalculate_repair_total_cost(OLD.repair_id);
        END IF;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER tr_vehicle_component_history_recalculate_total_cost
AFTER INSERT OR UPDATE OR DELETE ON vehicle_component_history
FOR EACH ROW EXECUTE FUNCTION trg_vehicle_component_history_recalculate_total_cost();

CREATE OR REPLACE FUNCTION trg_vehicle_driver_assignment_check_driver_position()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_position VARCHAR(100);
BEGIN
    SELECT e.position INTO v_position FROM employee e WHERE e.id = NEW.driver_employee_id;
    IF v_position IS NULL THEN
        RAISE EXCEPTION 'Сотрудник с id=% не найден', NEW.driver_employee_id;
    END IF;
    IF UPPER(v_position) <> 'DRIVER' THEN
        RAISE EXCEPTION 'Сотрудник с id=% не является водителем. Текущая должность: %', NEW.driver_employee_id, v_position;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_vehicle_driver_assignment_check_driver_position
BEFORE INSERT OR UPDATE ON vehicle_driver_assignment
FOR EACH ROW EXECUTE FUNCTION trg_vehicle_driver_assignment_check_driver_position();

CREATE OR REPLACE FUNCTION trg_route_vehicle_assignment_check_vehicle_category()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_route_type VARCHAR(50);
    v_vehicle_category VARCHAR(100);
BEGIN
    SELECT r.route_type INTO v_route_type FROM route r WHERE r.id = NEW.route_id;
    SELECT vc.name INTO v_vehicle_category
    FROM vehicle v JOIN vehicle_category vc ON vc.id = v.category_id
    WHERE v.id = NEW.vehicle_id;

    IF v_route_type IS NULL THEN
        RAISE EXCEPTION 'Маршрут с id=% не найден', NEW.route_id;
    END IF;
    IF v_vehicle_category IS NULL THEN
        RAISE EXCEPTION 'Транспортное средство с id=% не найдено или не имеет категории', NEW.vehicle_id;
    END IF;
    IF UPPER(v_route_type) <> UPPER(v_vehicle_category) THEN
        RAISE EXCEPTION 'Несоответствие маршрута и транспорта: route_type=%, vehicle_category=%', v_route_type, v_vehicle_category;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_route_vehicle_assignment_check_vehicle_category
BEFORE INSERT OR UPDATE ON route_vehicle_assignment
FOR EACH ROW EXECUTE FUNCTION trg_route_vehicle_assignment_check_vehicle_category();

CREATE OR REPLACE FUNCTION trg_transportation_record_check_consistency()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.record_type = 'PASSENGER' THEN
        IF NEW.route_id IS NULL THEN
            RAISE EXCEPTION 'Для PASSENGER записи route_id обязателен';
        END IF;
        IF NEW.passenger_count IS NULL THEN
            RAISE EXCEPTION 'Для PASSENGER записи passenger_count обязателен';
        END IF;
    ELSIF NEW.record_type = 'CARGO' THEN
        IF NEW.cargo_weight_kg IS NULL AND NEW.cargo_volume_m3 IS NULL THEN
            RAISE EXCEPTION 'Для CARGO записи должен быть указан cargo_weight_kg или cargo_volume_m3';
        END IF;
    ELSIF NEW.record_type = 'SERVICE' THEN
        IF NEW.hours_used IS NULL THEN
            RAISE EXCEPTION 'Для SERVICE записи hours_used обязателен';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_transportation_record_check_consistency
BEFORE INSERT OR UPDATE ON transportation_record
FOR EACH ROW EXECUTE FUNCTION trg_transportation_record_check_consistency();

CREATE OR REPLACE FUNCTION trg_vehicle_component_history_check_logic()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_last_action VARCHAR(50);
    v_last_vehicle_id BIGINT;
BEGIN
    SELECT h.action_type, h.vehicle_id
    INTO v_last_action, v_last_vehicle_id
    FROM vehicle_component_history h
    WHERE h.component_id = NEW.component_id
      AND (TG_OP <> 'UPDATE' OR h.id <> NEW.id)
    ORDER BY h.action_date DESC, h.id DESC
    LIMIT 1;

    IF NEW.action_type = 'INSTALLED' THEN
        IF v_last_action IN ('INSTALLED', 'REPAIRED') THEN
            RAISE EXCEPTION 'Агрегат id=% уже считается установленным на ТС id=%. Сначала его нужно снять или заменить', NEW.component_id, v_last_vehicle_id;
        END IF;
    ELSIF NEW.action_type IN ('REMOVED', 'REPLACED', 'REPAIRED') THEN
        IF v_last_action IS NULL OR v_last_action NOT IN ('INSTALLED', 'REPAIRED') THEN
            RAISE EXCEPTION 'Нельзя выполнить действие % для агрегата id=%, так как он не установлен', NEW.action_type, NEW.component_id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_vehicle_component_history_check_logic
BEFORE INSERT OR UPDATE ON vehicle_component_history
FOR EACH ROW EXECUTE FUNCTION trg_vehicle_component_history_check_logic();

CREATE OR REPLACE FUNCTION fn_refresh_component_status(p_component_id BIGINT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    v_last_action VARCHAR(50);
BEGIN
    SELECT h.action_type INTO v_last_action
    FROM vehicle_component_history h
    WHERE h.component_id = p_component_id
    ORDER BY h.action_date DESC, h.id DESC
    LIMIT 1;

    IF v_last_action IS NULL THEN
        UPDATE component SET status = 'IN_STOCK' WHERE id = p_component_id;
    ELSIF v_last_action IN ('INSTALLED', 'REPAIRED') THEN
        UPDATE component SET status = 'INSTALLED' WHERE id = p_component_id;
    ELSIF v_last_action IN ('REMOVED', 'REPLACED') THEN
        UPDATE component SET status = 'IN_STOCK' WHERE id = p_component_id;
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION trg_vehicle_component_history_refresh_component_status()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        PERFORM fn_refresh_component_status(NEW.component_id);
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.component_id IS DISTINCT FROM NEW.component_id THEN
            PERFORM fn_refresh_component_status(OLD.component_id);
        END IF;
        PERFORM fn_refresh_component_status(NEW.component_id);
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM fn_refresh_component_status(OLD.component_id);
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER tr_vehicle_component_history_refresh_component_status
AFTER INSERT OR UPDATE OR DELETE ON vehicle_component_history
FOR EACH ROW EXECUTE FUNCTION trg_vehicle_component_history_refresh_component_status();

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

CREATE OR REPLACE FUNCTION trg_vehicle_location_history_check_integrity()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_capacity INT;
    v_occupied INT;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM vehicle_location_history existing
        WHERE existing.vehicle_id = NEW.vehicle_id
          AND (TG_OP <> 'UPDATE' OR existing.id <> NEW.id)
          AND existing.start_date <= COALESCE(NEW.end_date, 'infinity'::date)
          AND COALESCE(existing.end_date, 'infinity'::date) >= NEW.start_date
    ) THEN
        RAISE EXCEPTION 'Период размещения транспорта id=% пересекается с уже существующей записью', NEW.vehicle_id;
    END IF;

    SELECT go.capacity
    INTO v_capacity
    FROM garage_object go
    WHERE go.id = NEW.garage_object_id;

    IF v_capacity IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT COUNT(*)::INT
    INTO v_occupied
    FROM vehicle_location_history existing
    WHERE existing.garage_object_id = NEW.garage_object_id
      AND (TG_OP <> 'UPDATE' OR existing.id <> NEW.id)
      AND existing.start_date <= COALESCE(NEW.end_date, 'infinity'::date)
      AND COALESCE(existing.end_date, 'infinity'::date) >= NEW.start_date;

    IF v_occupied + 1 > v_capacity THEN
        RAISE EXCEPTION 'Вместимость объекта гаражного хозяйства id=% превышена: занято %, вместимость %',
            NEW.garage_object_id, v_occupied + 1, v_capacity;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER tr_vehicle_location_history_check_integrity
BEFORE INSERT OR UPDATE ON vehicle_location_history
FOR EACH ROW EXECUTE FUNCTION trg_vehicle_location_history_check_integrity();
