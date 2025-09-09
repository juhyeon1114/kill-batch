DROP TABLE IF EXISTS victims;
CREATE TABLE victims
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255),
    process_id    VARCHAR(50),
    terminated_at TIMESTAMP,
    status        VARCHAR(20)
);

INSERT INTO victims (name, process_id, terminated_at, status)
VALUES ('zombie_process', 'PID_12345', '2024-01-01 12:00:00', 'TERMINATED'),
       ('sleeping_thread', 'PID_45678', '2024-01-15 15:30:00', 'TERMINATED'),
       ('memory_leak', 'PID_98765', '2024-02-01 09:15:00', 'RUNNING'),
       ('infinite_loop', 'PID_24680', '2024-02-15 18:45:00', 'RUNNING'),
       ('deadlock_case', 'PID_13579', '2024-03-01 22:10:00', 'TERMINATED'),
       ('orphan_thread', 'PID_11223', '2024-03-10 11:05:00', 'TERMINATED'),
       ('cpu_hog', 'PID_44556', '2024-04-05 08:20:00', 'RUNNING'),
       ('disk_io_wait', 'PID_77889', '2024-04-15 14:40:00', 'RUNNING'),
       ('hung_service', 'PID_99001', '2024-05-01 07:30:00', 'TERMINATED'),
       ('socket_leak', 'PID_22334', '2024-05-20 17:25:00', 'TERMINATED'),
       ('abandoned_mutex', 'PID_55667', '2024-06-05 19:00:00', 'RUNNING'),
       ('stuck_queue', 'PID_88990', '2024-06-18 10:10:00', 'RUNNING'),
       ('segfault', 'PID_10293', '2024-07-03 13:50:00', 'TERMINATED'),
       ('oom_killer', 'PID_74839', '2024-07-15 16:45:00', 'TERMINATED'),
       ('race_condition', 'PID_56172', '2024-08-01 12:05:00', 'RUNNING'),
       ('dead_task', 'PID_32459', '2024-08-10 21:20:00', 'TERMINATED'),
       ('thread_pool_exhaustion', 'PID_95126', '2024-09-01 23:55:00', 'RUNNING'),
       ('database_lock', 'PID_68542', '2024-09-15 06:35:00', 'RUNNING'),
       ('unhandled_exception', 'PID_29741', '2024-10-01 04:25:00', 'TERMINATED'),
       ('corrupt_memory', 'PID_14378', '2024-10-20 15:55:00', 'TERMINATED');


-- 테이블 생성
DROP TABLE IF EXISTS orders;
CREATE TABLE orders
(
    id             BIGSERIAL PRIMARY KEY,
    customer_id    BIGINT      NOT NULL,
    order_datetime TIMESTAMP   NOT NULL,
    status         VARCHAR(20) NOT NULL,
    shipping_id    VARCHAR(50)
);

-- 테스트 데이터 삽입
INSERT INTO orders (customer_id, order_datetime, status, shipping_id)
SELECT floor(random() * 100 + 1),
       NOW() - (random() * INTERVAL '30 days'),
       CASE
           WHEN rn <= 10 THEN 'READY_FOR_SHIPMENT'
           WHEN rn <= 20 THEN 'SHIPPED'
           WHEN rn <= 20 THEN 'CANCELLED'
           ELSE 'CANCELLED'
           END,
       CASE
           WHEN rn <= 10 THEN NULL
           WHEN rn <= 20 THEN NULL
           ELSE 'SHIP-' || LPAD(CAST(rn AS VARCHAR), 8, '0')
           END
FROM (SELECT GENERATE_SERIES(1, 30) AS rn) AS series;
