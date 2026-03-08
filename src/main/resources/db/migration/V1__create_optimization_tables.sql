CREATE TABLE optimized_outputs (
    id              BIGSERIAL PRIMARY KEY,
    request_id      UUID NOT NULL UNIQUE,
    total_volume    INTEGER NOT NULL,
    total_revenue   BIGINT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_optimized_outputs_request_id ON optimized_outputs (request_id);
CREATE INDEX idx_optimized_outputs_created_at ON optimized_outputs (created_at DESC);

CREATE TABLE selected_shipments (
    id                   BIGSERIAL PRIMARY KEY,
    optimized_output_id  BIGINT NOT NULL REFERENCES optimized_outputs (id) ON DELETE CASCADE,
    name                 VARCHAR(255) NOT NULL,
    volume               INTEGER NOT NULL,
    revenue              BIGINT NOT NULL
);

CREATE INDEX idx_selected_shipments_optimized_output_id ON selected_shipments (optimized_output_id);

CREATE TABLE optimization_inputs (
    id          BIGSERIAL PRIMARY KEY,
    request_id  UUID NOT NULL UNIQUE,
    max_volume  INTEGER NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_optimization_inputs_request_id ON optimization_inputs (request_id);
CREATE INDEX idx_optimization_inputs_created_at ON optimization_inputs (created_at DESC);

CREATE TABLE input_shipments (
    id                    BIGSERIAL PRIMARY KEY,
    optimization_input_id BIGINT NOT NULL REFERENCES optimization_inputs (id) ON DELETE CASCADE,
    name                  VARCHAR(255) NOT NULL,
    volume                INTEGER NOT NULL,
    revenue               BIGINT NOT NULL
);

CREATE INDEX idx_input_shipments_optimization_input_id ON input_shipments (optimization_input_id);
