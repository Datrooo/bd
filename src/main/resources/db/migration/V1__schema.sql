CREATE TABLE vehicle_category (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE vehicle (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    inventory_number VARCHAR(50) NOT NULL UNIQUE,
    registration_number VARCHAR(20) NOT NULL UNIQUE,
    vin VARCHAR(50) UNIQUE,
    category_id BIGINT NOT NULL,
    brand_name VARCHAR(100) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    manufacture_year INT,
    purchase_date DATE,
    commissioning_date DATE,
    current_mileage NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    passenger_capacity INT,
    load_capacity_kg NUMERIC(12,2),
    cargo_volume_m3 NUMERIC(12,2),
    body_type VARCHAR(100),
    taxi_license_number VARCHAR(100),
    service_purpose VARCHAR(200),
    color VARCHAR(50),
    engine_number VARCHAR(50),
    chassis_number VARCHAR(50),
    notes TEXT,
    CONSTRAINT fk_vehicle_category FOREIGN KEY (category_id) REFERENCES vehicle_category(id),
    CONSTRAINT chk_vehicle_current_mileage CHECK (current_mileage >= 0),
    CONSTRAINT chk_vehicle_manufacture_year CHECK (
        manufacture_year IS NULL
        OR manufacture_year BETWEEN 1900 AND EXTRACT(YEAR FROM CURRENT_DATE)::INT + 1
    ),
    CONSTRAINT chk_vehicle_passenger_capacity CHECK (passenger_capacity IS NULL OR passenger_capacity >= 0),
    CONSTRAINT chk_vehicle_load_capacity CHECK (load_capacity_kg IS NULL OR load_capacity_kg >= 0),
    CONSTRAINT chk_vehicle_cargo_volume CHECK (cargo_volume_m3 IS NULL OR cargo_volume_m3 >= 0),
    CONSTRAINT chk_vehicle_status CHECK (status IN ('ACTIVE', 'IN_REPAIR', 'WRITTEN_OFF', 'SOLD', 'RESERVE'))
);

CREATE TABLE vehicle_acquisition (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    acquisition_date DATE NOT NULL,
    acquisition_type VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(150),
    document_number VARCHAR(100),
    cost NUMERIC(14,2),
    notes TEXT,
    CONSTRAINT fk_vehicle_acquisition_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT chk_vehicle_acquisition_type CHECK (acquisition_type IN ('PURCHASE', 'TRANSFER', 'LEASE')),
    CONSTRAINT chk_vehicle_acquisition_cost CHECK (cost IS NULL OR cost >= 0)
);

CREATE TABLE vehicle_disposal (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    disposal_date DATE NOT NULL,
    disposal_type VARCHAR(50) NOT NULL,
    reason TEXT,
    document_number VARCHAR(100),
    amount_received NUMERIC(14,2),
    notes TEXT,
    CONSTRAINT fk_vehicle_disposal_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT chk_vehicle_disposal_type CHECK (disposal_type IN ('WRITE_OFF', 'SALE', 'TRANSFER')),
    CONSTRAINT chk_vehicle_disposal_amount_received CHECK (amount_received IS NULL OR amount_received >= 0)
);

CREATE TABLE employee (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    personnel_number VARCHAR(50) NOT NULL UNIQUE,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    birth_date DATE,
    hire_date DATE NOT NULL,
    dismissal_date DATE,
    position VARCHAR(100) NOT NULL,
    qualification VARCHAR(200),
    phone VARCHAR(30),
    email VARCHAR(150),
    address TEXT,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    CONSTRAINT chk_employee_status CHECK (status IN ('ACTIVE', 'VACATION', 'SICK_LEAVE', 'DISMISSED')),
    CONSTRAINT chk_employee_dates CHECK (dismissal_date IS NULL OR dismissal_date >= hire_date)
);

CREATE TABLE workshop (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    chief_employee_id BIGINT,
    description TEXT,
    CONSTRAINT fk_workshop_chief_employee FOREIGN KEY (chief_employee_id) REFERENCES employee(id)
);

CREATE TABLE section (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workshop_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    master_employee_id BIGINT,
    description TEXT,
    CONSTRAINT fk_section_workshop FOREIGN KEY (workshop_id) REFERENCES workshop(id),
    CONSTRAINT fk_section_master_employee FOREIGN KEY (master_employee_id) REFERENCES employee(id),
    CONSTRAINT uq_section_workshop_name UNIQUE (workshop_id, name)
);

CREATE TABLE brigade (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    section_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    brigadier_employee_id BIGINT,
    description TEXT,
    CONSTRAINT fk_brigade_section FOREIGN KEY (section_id) REFERENCES section(id),
    CONSTRAINT fk_brigade_brigadier_employee FOREIGN KEY (brigadier_employee_id) REFERENCES employee(id),
    CONSTRAINT uq_brigade_section_name UNIQUE (section_id, name)
);

CREATE TABLE employee_brigade_assignment (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    brigade_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_employee_brigade_assignment_employee FOREIGN KEY (employee_id) REFERENCES employee(id),
    CONSTRAINT fk_employee_brigade_assignment_brigade FOREIGN KEY (brigade_id) REFERENCES brigade(id),
    CONSTRAINT chk_employee_brigade_assignment_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TABLE vehicle_driver_assignment (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    driver_employee_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    assignment_type VARCHAR(50) NOT NULL,
    notes TEXT,
    CONSTRAINT fk_vehicle_driver_assignment_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT fk_vehicle_driver_assignment_employee FOREIGN KEY (driver_employee_id) REFERENCES employee(id),
    CONSTRAINT chk_vehicle_driver_assignment_type CHECK (assignment_type IN ('PRIMARY', 'RESERVE', 'SHIFT')),
    CONSTRAINT chk_vehicle_driver_assignment_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TABLE garage_object (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    object_type VARCHAR(50) NOT NULL,
    parent_object_id BIGINT,
    workshop_id BIGINT,
    section_id BIGINT,
    address VARCHAR(255),
    capacity INT,
    description TEXT,
    CONSTRAINT fk_garage_object_parent FOREIGN KEY (parent_object_id) REFERENCES garage_object(id),
    CONSTRAINT fk_garage_object_workshop FOREIGN KEY (workshop_id) REFERENCES workshop(id),
    CONSTRAINT fk_garage_object_section FOREIGN KEY (section_id) REFERENCES section(id),
    CONSTRAINT chk_garage_object_type CHECK (object_type IN ('GARAGE', 'BOX', 'REPAIR_BUILDING', 'PARKING', 'WAREHOUSE')),
    CONSTRAINT chk_garage_object_capacity CHECK (capacity IS NULL OR capacity >= 0)
);

CREATE TABLE vehicle_location_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    garage_object_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    notes TEXT,
    CONSTRAINT fk_vehicle_location_history_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT fk_vehicle_location_history_garage_object FOREIGN KEY (garage_object_id) REFERENCES garage_object(id),
    CONSTRAINT chk_vehicle_location_history_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TABLE route (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    route_number VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    route_type VARCHAR(50) NOT NULL,
    start_point VARCHAR(150) NOT NULL,
    end_point VARCHAR(150) NOT NULL,
    length_km NUMERIC(10,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    CONSTRAINT uq_route_number_type UNIQUE (route_number, route_type),
    CONSTRAINT chk_route_type CHECK (route_type IN ('BUS', 'MINIBUS')),
    CONSTRAINT chk_route_length CHECK (length_km IS NULL OR length_km >= 0)
);

CREATE TABLE route_vehicle_assignment (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    route_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    shift_info VARCHAR(100),
    notes TEXT,
    CONSTRAINT fk_route_vehicle_assignment_route FOREIGN KEY (route_id) REFERENCES route(id),
    CONSTRAINT fk_route_vehicle_assignment_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT chk_route_vehicle_assignment_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TABLE transportation_record (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    route_id BIGINT,
    record_type VARCHAR(50) NOT NULL,
    record_date DATE NOT NULL,
    mileage_km NUMERIC(12,2) NOT NULL DEFAULT 0,
    hours_used NUMERIC(10,2),
    passenger_count INT,
    cargo_weight_kg NUMERIC(12,2),
    cargo_volume_m3 NUMERIC(12,2),
    trip_count INT NOT NULL DEFAULT 1,
    revenue NUMERIC(14,2),
    description TEXT,
    notes TEXT,
    CONSTRAINT fk_transportation_record_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT fk_transportation_record_route FOREIGN KEY (route_id) REFERENCES route(id),
    CONSTRAINT chk_transportation_record_type CHECK (record_type IN ('PASSENGER', 'CARGO', 'SERVICE')),
    CONSTRAINT chk_transportation_record_mileage CHECK (mileage_km >= 0),
    CONSTRAINT chk_transportation_record_hours_used CHECK (hours_used IS NULL OR hours_used >= 0),
    CONSTRAINT chk_transportation_record_passenger_count CHECK (passenger_count IS NULL OR passenger_count >= 0),
    CONSTRAINT chk_transportation_record_cargo_weight CHECK (cargo_weight_kg IS NULL OR cargo_weight_kg >= 0),
    CONSTRAINT chk_transportation_record_cargo_volume CHECK (cargo_volume_m3 IS NULL OR cargo_volume_m3 >= 0),
    CONSTRAINT chk_transportation_record_trip_count CHECK (trip_count > 0),
    CONSTRAINT chk_transportation_record_revenue CHECK (revenue IS NULL OR revenue >= 0)
);

CREATE TABLE repair_type (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE repair (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    repair_type_id BIGINT NOT NULL,
    workshop_id BIGINT,
    section_id BIGINT,
    brigade_id BIGINT,
    start_date DATE NOT NULL,
    end_date DATE,
    reason TEXT,
    description TEXT,
    total_cost NUMERIC(14,2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    CONSTRAINT fk_repair_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT fk_repair_type FOREIGN KEY (repair_type_id) REFERENCES repair_type(id),
    CONSTRAINT fk_repair_workshop FOREIGN KEY (workshop_id) REFERENCES workshop(id),
    CONSTRAINT fk_repair_section FOREIGN KEY (section_id) REFERENCES section(id),
    CONSTRAINT fk_repair_brigade FOREIGN KEY (brigade_id) REFERENCES brigade(id),
    CONSTRAINT chk_repair_dates CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT chk_repair_total_cost CHECK (total_cost >= 0),
    CONSTRAINT chk_repair_status CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE repair_work (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    repair_id BIGINT NOT NULL,
    employee_id BIGINT,
    work_type VARCHAR(150) NOT NULL,
    description TEXT,
    quantity NUMERIC(12,2) NOT NULL DEFAULT 1,
    cost NUMERIC(14,2) NOT NULL DEFAULT 0,
    completed_at TIMESTAMP,
    notes TEXT,
    CONSTRAINT fk_repair_work_repair FOREIGN KEY (repair_id) REFERENCES repair(id),
    CONSTRAINT fk_repair_work_employee FOREIGN KEY (employee_id) REFERENCES employee(id),
    CONSTRAINT chk_repair_work_quantity CHECK (quantity > 0),
    CONSTRAINT chk_repair_work_cost CHECK (cost >= 0)
);

CREATE TABLE component (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    component_type VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    model VARCHAR(100),
    manufacturer VARCHAR(100),
    production_date DATE,
    purchase_date DATE,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    CONSTRAINT chk_component_status CHECK (status IN ('IN_STOCK', 'INSTALLED', 'UNDER_REPAIR', 'WRITTEN_OFF'))
);

CREATE TABLE vehicle_component_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    component_id BIGINT NOT NULL,
    repair_id BIGINT,
    action_type VARCHAR(50) NOT NULL,
    action_date DATE NOT NULL,
    cost NUMERIC(14,2) NOT NULL DEFAULT 0,
    notes TEXT,
    CONSTRAINT fk_vehicle_component_history_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT fk_vehicle_component_history_component FOREIGN KEY (component_id) REFERENCES component(id),
    CONSTRAINT fk_vehicle_component_history_repair FOREIGN KEY (repair_id) REFERENCES repair(id),
    CONSTRAINT chk_vehicle_component_history_action_type CHECK (action_type IN ('INSTALLED', 'REMOVED', 'REPLACED', 'REPAIRED')),
    CONSTRAINT chk_vehicle_component_history_cost CHECK (cost >= 0)
);

CREATE TABLE role (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE app_user (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    employee_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP,
    CONSTRAINT fk_app_user_employee FOREIGN KEY (employee_id) REFERENCES employee(id),
    CONSTRAINT chk_app_user_last_login CHECK (last_login_at IS NULL OR last_login_at >= created_at)
);

CREATE TABLE user_role (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_vehicle_category_id ON vehicle(category_id);
CREATE INDEX idx_vehicle_acquisition_date ON vehicle_acquisition(acquisition_date);
CREATE INDEX idx_vehicle_disposal_date ON vehicle_disposal(disposal_date);
CREATE INDEX idx_employee_position ON employee(position);
CREATE INDEX idx_employee_status ON employee(status);
CREATE INDEX idx_employee_brigade_assignment_employee ON employee_brigade_assignment(employee_id);
CREATE INDEX idx_employee_brigade_assignment_brigade ON employee_brigade_assignment(brigade_id);
CREATE INDEX idx_vehicle_driver_assignment_vehicle ON vehicle_driver_assignment(vehicle_id);
CREATE INDEX idx_vehicle_driver_assignment_driver ON vehicle_driver_assignment(driver_employee_id);
CREATE INDEX idx_vehicle_location_history_vehicle ON vehicle_location_history(vehicle_id);
CREATE INDEX idx_vehicle_location_history_garage_object ON vehicle_location_history(garage_object_id);
CREATE INDEX idx_route_vehicle_assignment_route ON route_vehicle_assignment(route_id);
CREATE INDEX idx_route_vehicle_assignment_vehicle ON route_vehicle_assignment(vehicle_id);
CREATE INDEX idx_transportation_record_date ON transportation_record(record_date);
CREATE INDEX idx_transportation_record_vehicle ON transportation_record(vehicle_id);
CREATE INDEX idx_transportation_record_route ON transportation_record(route_id);
CREATE INDEX idx_transportation_record_type ON transportation_record(record_type);
CREATE INDEX idx_repair_start_date ON repair(start_date);
CREATE INDEX idx_repair_end_date ON repair(end_date);
CREATE INDEX idx_repair_vehicle ON repair(vehicle_id);
CREATE INDEX idx_repair_repair_type ON repair(repair_type_id);
CREATE INDEX idx_repair_brigade ON repair(brigade_id);
CREATE INDEX idx_repair_work_repair ON repair_work(repair_id);
CREATE INDEX idx_repair_work_employee ON repair_work(employee_id);
CREATE INDEX idx_vehicle_component_history_date ON vehicle_component_history(action_date);
CREATE INDEX idx_vehicle_component_history_vehicle ON vehicle_component_history(vehicle_id);
CREATE INDEX idx_vehicle_component_history_component ON vehicle_component_history(component_id);
CREATE INDEX idx_user_role_user ON user_role(user_id);
CREATE INDEX idx_user_role_role ON user_role(role_id);
