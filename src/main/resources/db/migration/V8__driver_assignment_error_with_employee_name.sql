CREATE OR REPLACE FUNCTION trg_vehicle_driver_assignment_check_driver_position()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_position VARCHAR(100);
    v_employee_name TEXT;
BEGIN
    SELECT
        e.position,
        CONCAT_WS(' ', e.last_name, e.first_name, e.middle_name)
    INTO v_position, v_employee_name
    FROM employee e
    WHERE e.id = NEW.driver_employee_id;

    IF v_position IS NULL THEN
        RAISE EXCEPTION 'Выбранный сотрудник не найден';
    END IF;

    IF UPPER(v_position) <> 'DRIVER' THEN
        RAISE EXCEPTION 'Сотрудник % не является водителем. Текущая должность: %',
            v_employee_name,
            v_position;
    END IF;

    RETURN NEW;
END;
$$;
