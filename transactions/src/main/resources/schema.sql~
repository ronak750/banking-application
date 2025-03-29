-- CREATE TABLE IF NOT EXISTS `upis` (
--     `upi_id` varchar(100) PRIMARY KEY,
--     `user_id` int NOT NULL,
--     `is_active` varchar(100) NOT NULL,
--     `created_at` date NOT NULL
--     );

-- CREATE TABLE IF NOT EXISTS `wallets` (
--     `wallet_id` int PRIMARY KEY,
--     `user_id` int NOT NULL,
--     `is_active` varchar(100) NOT NULL,
--     `created_at` date NOT NULL
--     )
--
--     ;

CREATE TABLE IF NOT EXISTS `transactions` (
    `transaction_id` varchar(100) PRIMARY KEY,
    `transaction_type`  varchar(100) NOT NULL,
    `source_id` varchar(100) NOT NULL,
    `destination_id` varchar(100) NOT NULL,
    `amount` decimal NOT NULL,
    `transaction_status` varchar(100) NOT NULL,
    `description` varchar(500),
    `created_at` date NOT NULL,
    `updated_at` date DEFAULT NULL
    );

CREATE TABLE IF NOT EXISTS `neft_processing_queue` (
    `transaction_id` varchar(100) PRIMARY KEY,
    `from_account_number`  varchar(100) NOT NULL,
    `from_account_ifsc` varchar(100) NOT NULL,
    `to_account_number` varchar(100) NOT NULL,
    `to_account_ifsc` varchar(100) NOT NULL,
    `amount` decimal NOT NULL,
    `scheduled_at` date NOT NULL,
    `submitted_by` varchar(100) NOT NULL
    );