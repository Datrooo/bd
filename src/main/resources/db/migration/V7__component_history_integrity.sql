CREATE OR REPLACE FUNCTION trg_vehicle_component_history_check_logic()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_previous_action VARCHAR(50);
    v_previous_vehicle_id BIGINT;
    v_next_action VARCHAR(50);
    v_next_vehicle_id BIGINT;
    v_repair_vehicle_id BIGINT;
    v_repair_start_date DATE;
    v_repair_end_date DATE;
BEGIN
    IF NEW.repair_id IS NOT NULL THEN
        SELECT r.vehicle_id, r.start_date, r.end_date
        INTO v_repair_vehicle_id, v_repair_start_date, v_repair_end_date
        FROM repair r
        WHERE r.id = NEW.repair_id;

        IF v_repair_vehicle_id IS DISTINCT FROM NEW.vehicle_id THEN
            RAISE EXCEPTION 'Выбранный ремонт относится к другому автомобилю';
        END IF;

        IF NEW.action_date < v_repair_start_date
            OR (v_repair_end_date IS NOT NULL AND NEW.action_date > v_repair_end_date) THEN
            RAISE EXCEPTION 'Дата действия должна входить в период выбранного ремонта';
        END IF;
    END IF;

    IF NEW.action_type IN ('REPAIRED', 'REPLACED') AND NEW.repair_id IS NULL THEN
        RAISE EXCEPTION 'Для действия % необходимо выбрать ремонт', NEW.action_type;
    END IF;

    SELECT h.action_type, h.vehicle_id
    INTO v_previous_action, v_previous_vehicle_id
    FROM vehicle_component_history h
    WHERE h.component_id = NEW.component_id
      AND (TG_OP <> 'UPDATE' OR h.id <> NEW.id)
      AND (
          h.action_date < NEW.action_date
          OR (h.action_date = NEW.action_date AND h.id < NEW.id)
      )
    ORDER BY h.action_date DESC, h.id DESC
    LIMIT 1;

    SELECT h.action_type, h.vehicle_id
    INTO v_next_action, v_next_vehicle_id
    FROM vehicle_component_history h
    WHERE h.component_id = NEW.component_id
      AND (TG_OP <> 'UPDATE' OR h.id <> NEW.id)
      AND (
          h.action_date > NEW.action_date
          OR (h.action_date = NEW.action_date AND h.id > NEW.id)
      )
    ORDER BY h.action_date, h.id
    LIMIT 1;

    IF NEW.action_type = 'INSTALLED' THEN
        IF v_previous_action IN ('INSTALLED', 'REPAIRED') THEN
            RAISE EXCEPTION 'Агрегат уже установлен на автомобиле id=%. Сначала его нужно снять или заменить', v_previous_vehicle_id;
        END IF;
    ELSE
        IF v_previous_action IS NULL OR v_previous_action NOT IN ('INSTALLED', 'REPAIRED') THEN
            RAISE EXCEPTION 'Действие % невозможно: перед ним агрегат не был установлен', NEW.action_type;
        END IF;

        IF v_previous_vehicle_id IS DISTINCT FROM NEW.vehicle_id THEN
            RAISE EXCEPTION 'Агрегат установлен на другом автомобиле id=%', v_previous_vehicle_id;
        END IF;
    END IF;

    IF v_next_action IS NOT NULL THEN
        IF NEW.action_type IN ('INSTALLED', 'REPAIRED') THEN
            IF v_next_action = 'INSTALLED' THEN
                RAISE EXCEPTION 'После этого действия в истории уже есть повторная установка агрегата';
            END IF;

            IF v_next_vehicle_id IS DISTINCT FROM NEW.vehicle_id THEN
                RAISE EXCEPTION 'Следующее действие истории относится к другому автомобилю';
            END IF;
        ELSIF v_next_action <> 'INSTALLED' THEN
            RAISE EXCEPTION 'После снятия или замены следующим действием может быть только установка';
        END IF;
    END IF;

    RETURN NEW;
END;
$$;
