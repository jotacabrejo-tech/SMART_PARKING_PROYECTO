-- =============================================================================
-- SMART PARKING — Esquema SQL de Referencia (MySQL 9.7.1)
-- Evidencia SENA: GA7-220501096-AA2-EV02 — Modulos de software codificados y probados
-- Nota: Archivo de referencia estructural sin datos personales ni credenciales.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `smart_parking`
  DEFAULT CHARACTER SET utf8mb3;

USE `smart_parking`;

-- 1. Tabla: registro_parqueo
CREATE TABLE IF NOT EXISTS `registro_parqueo` (
  `id_registro` int NOT NULL AUTO_INCREMENT,
  `placa` varchar(10) NOT NULL,
  `hora_ingreso` datetime NOT NULL,
  `valor_calculado` decimal(10,2) NOT NULL,
  `estado_pago` varchar(20) NOT NULL,
  PRIMARY KEY (`id_registro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- 2. Tabla: cliente
CREATE TABLE IF NOT EXISTS `cliente` (
  `id_cliente` int NOT NULL AUTO_INCREMENT,
  `tipo_doc` varchar(20) NOT NULL,
  `num_doc` varchar(20) NOT NULL,
  `nombres` varchar(100) NOT NULL,
  `correo` varchar(100) NOT NULL,
  PRIMARY KEY (`id_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- 3. Tabla: pago
CREATE TABLE IF NOT EXISTS `pago` (
  `id_pago` int NOT NULL AUTO_INCREMENT,
  `id_registro` int NOT NULL,
  `fecha_pago` datetime NOT NULL,
  `valor_pagado` decimal(10,2) NOT NULL,
  `estado_pago` varchar(20) NOT NULL,
  `tiempo_limite_salida` int NOT NULL,
  PRIMARY KEY (`id_pago`),
  KEY `fk_pago_registro_idx` (`id_registro`),
  CONSTRAINT `fk_pago_registro` FOREIGN KEY (`id_registro`) REFERENCES `registro_parqueo` (`id_registro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

