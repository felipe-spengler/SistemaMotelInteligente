-- Cria a tabela de periodos por quarto
CREATE TABLE IF NOT EXISTS `periodos_quarto` (
  `id` int NOT NULL AUTO_INCREMENT,
  `numeroquarto` int NOT NULL,
  `descricao` varchar(50) NOT NULL,
  `tempo_minutos` int NOT NULL,
  `valor` float NOT NULL,
  `is_pernoite` tinyint(1) DEFAULT '0',
  `ordem` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_periodos_quarto` (`numeroquarto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Adiciona a coluna para saber qual periodo o cliente usou na locacao
ALTER TABLE `registralocado` ADD COLUMN `periodo_locado` VARCHAR(50) DEFAULT NULL;
