-- User table
CREATE TABLE User
(
    user_id    bigint AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    sex        VARCHAR(50),
    age        INT,
    height     INT,
    weight     INT,
    how_active VARCHAR(50)
);

-- Meal table
CREATE TABLE Meal
(
    meal_id       bigint AUTO_INCREMENT PRIMARY KEY,
    user_id       bigint,
    meal_name     VARCHAR(255) NOT NULL,
    calories      INT,
    protein       FLOAT,
    fat           FLOAT,
    carbohydrates FLOAT,
    date          DATE         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User (user_id)
);

-- Product table
CREATE TABLE Product
(
    product_id    bigint AUTO_INCREMENT PRIMARY KEY,
    user_id       bigint,
    product_name  VARCHAR(255) NOT NULL,
    calories      INT,
    protein       FLOAT,
    fat           FLOAT,
    carbohydrates FLOAT,
    FOREIGN KEY (user_id) REFERENCES User (user_id)
);

-- MealProduct table
CREATE TABLE MealProduct
(
    meal_id    bigint,
    product_id bigint,
    quantity   FLOAT,
    PRIMARY KEY (meal_id, product_id),
    FOREIGN KEY (meal_id) REFERENCES Meal (meal_id),
    FOREIGN KEY (product_id) REFERENCES Product (product_id)
);