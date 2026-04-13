-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 145.223.30.211    Database: u876938716_motel
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `alarmes`
--

DROP TABLE IF EXISTS `alarmes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alarmes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `hora_adicionado` timestamp NOT NULL,
  `hora_despertar` timestamp NOT NULL,
  `descricao` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ativo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `antecipado`
--

DROP TABLE IF EXISTS `antecipado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `antecipado` (
  `id` int NOT NULL AUTO_INCREMENT,
  `idlocacao` int DEFAULT NULL,
  `tipo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `valor` float DEFAULT NULL,
  `hora` timestamp NULL DEFAULT NULL,
  `idcaixaatual` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=660 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `caixa`
--

DROP TABLE IF EXISTS `caixa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caixa` (
  `id` int NOT NULL AUTO_INCREMENT,
  `horaabre` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `usuarioabre` varchar(45) NOT NULL,
  `saldoabre` float NOT NULL,
  `horafecha` timestamp NULL DEFAULT NULL,
  `usuariofecha` varchar(45) DEFAULT NULL,
  `saldofecha` float DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_abertura_simultanea` (`horaabre`,`usuarioabre`)
) ENGINE=InnoDB AUTO_INCREMENT=734 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comandos_pendentes`
--

DROP TABLE IF EXISTS `comandos_pendentes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comandos_pendentes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_unidade` int NOT NULL,
  `comando` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `executado` tinyint(1) DEFAULT '0',
  `criado_em` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuracoes`
--

DROP TABLE IF EXISTS `configuracoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuracoes` (
  `logoffcaixa` tinyint(1) DEFAULT NULL,
  `estoque` tinyint(1) DEFAULT NULL,
  `sistemaescolhe` tinyint(1) DEFAULT NULL,
  `meuip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `flagMesmoUserCaixa` tinyint(1) DEFAULT NULL,
  `limitadesconto` int DEFAULT NULL,
  `isRunning` tinyint(1) DEFAULT NULL,
  `conexoes` int DEFAULT '0',
  `id` int NOT NULL,
  `telaMostrar` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `portoesrf` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `desistencia`
--

DROP TABLE IF EXISTS `desistencia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `desistencia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `numquarto` int DEFAULT NULL,
  `horainicio` timestamp NULL DEFAULT NULL,
  `horafim` timestamp NULL DEFAULT NULL,
  `motivo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `idcaixaatual` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=194 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `funcionario`
--

DROP TABLE IF EXISTS `funcionario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `funcionario` (
  `idfuncionario` int NOT NULL AUTO_INCREMENT,
  `nomefuncionario` varchar(45) NOT NULL,
  `cargofuncionario` varchar(45) NOT NULL,
  `loginfuncionario` varchar(45) NOT NULL,
  `senhafuncionario` varchar(50) NOT NULL,
  PRIMARY KEY (`idfuncionario`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `imagens`
--

DROP TABLE IF EXISTS `imagens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `imagens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome_da_imagem` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `imagem` mediumblob,
  `data_de_armazenamento` timestamp NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `justificativa`
--

DROP TABLE IF EXISTS `justificativa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `justificativa` (
  `id` int NOT NULL AUTO_INCREMENT,
  `idlocacao` int DEFAULT NULL,
  `valor` float DEFAULT NULL,
  `tipo` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `justificativa` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=951 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `log_sincronizacao`
--

DROP TABLE IF EXISTS `log_sincronizacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `log_sincronizacao` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tabela_nome` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `registro_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_sincronizacao` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `operacao` enum('INSERT','UPDATE','DELETE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `log_sincronizacao_online`
--

DROP TABLE IF EXISTS `log_sincronizacao_online`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `log_sincronizacao_online` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tabela_nome` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `registro_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_sincronizacao` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `operacao` enum('INSERT','UPDATE','DELETE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=186 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `login_acesso`
--

DROP TABLE IF EXISTS `login_acesso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `login_acesso` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome_usuario` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pagina_acesso` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_acesso` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10612 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `login_registros`
--

DROP TABLE IF EXISTS `login_registros`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `login_registros` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome_usuario` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `senha_usuario` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_login` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3148 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mensalidade`
--

DROP TABLE IF EXISTS `mensalidade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mensalidade` (
  `id` int NOT NULL AUTO_INCREMENT,
  `referente` date NOT NULL COMMENT 'Mês/Ano que a mensalidade se refere',
  `metodo` enum('pix','cartao') COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Método de pagamento',
  `status` enum('pending','approved','rejected','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'Status do pagamento',
  `valor` decimal(10,2) NOT NULL,
  `transaction_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ID da transação no Mercado Pago',
  `qr_code` text COLLATE utf8mb4_unicode_ci COMMENT 'URL do QR Code (para PIX)',
  `qr_code_base64` longtext COLLATE utf8mb4_unicode_ci COMMENT 'Imagem do QR Code em Base64',
  `card_last_digits` varchar(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Últimos 4 dígitos do cartão',
  `external_reference` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Referência externa (pode ser o ID do sistema)',
  `criado_em` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transaction_id` (`transaction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pagamento_mensalidade`
--

DROP TABLE IF EXISTS `pagamento_mensalidade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pagamento_mensalidade` (
  `id` int NOT NULL AUTO_INCREMENT,
  `motel_slug` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `referencia_mes_ano` date NOT NULL,
  `valor_base` decimal(10,2) NOT NULL,
  `valor_multa` decimal(10,2) DEFAULT '0.00',
  `valor_final` decimal(10,2) NOT NULL,
  `metodo` enum('pix','cartao','boleto') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('pending','approved','rejected','cancelled','manual') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `transaction_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `external_reference` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `qr_code` text COLLATE utf8mb4_unicode_ci,
  `qr_code_base64` longtext COLLATE utf8mb4_unicode_ci,
  `card_last_digits` varchar(4) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `criado_em` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `portoes`
--

DROP TABLE IF EXISTS `portoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `portoes` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `portao` int NOT NULL,
  `codigo` int NOT NULL,
  `bitLength` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prevendidos`
--

DROP TABLE IF EXISTS `prevendidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prevendidos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `idlocacao` int NOT NULL,
  `idproduto` int NOT NULL,
  `quantidade` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=952 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `produtos`
--

DROP TABLE IF EXISTS `produtos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produtos` (
  `idproduto` int NOT NULL,
  `descricao` varchar(60) NOT NULL,
  `valorproduto` float NOT NULL,
  `estoque` char(20) DEFAULT NULL,
  `ultimacompra` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`idproduto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `periodos_dinamicos`
--

DROP TABLE IF EXISTS `periodos_dinamicos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `periodos_dinamicos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `descricao` varchar(50) NOT NULL, -- Ex: 1 Hora, 2 Horas, 3 Horas, Pernoite
  `tempo_minutos` int NOT NULL, -- O tempo de tolerância máxima no sistema (ex 60, 120...)
  `is_pernoite` tinyint(1) DEFAULT '0',
  `ordem_transicao` int NOT NULL, -- Ajuda o sistema a saber para qual período pular depois
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quarto_valores_periodo`
--

DROP TABLE IF EXISTS `quarto_valores_periodo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quarto_valores_periodo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `numeroquarto` int NOT NULL,
  `id_periodo` int NOT NULL,
  `valor` float NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_numeroquarto` (`numeroquarto`),
  KEY `fk_id_periodo` (`id_periodo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quartos`
--

DROP TABLE IF EXISTS `quartos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quartos` (
  `tipoquarto` varchar(45) NOT NULL,
  `numeroquarto` int NOT NULL,
  `valorquarto` float NOT NULL,
  `pernoitequarto` float NOT NULL,
  `addPessoa` float DEFAULT NULL,
  PRIMARY KEY (`numeroquarto`),
  KEY `tipoquarto` (`tipoquarto`,`numeroquarto`),
  KEY `tipoquarto_2` (`tipoquarto`,`numeroquarto`),
  KEY `tipoquarto_3` (`tipoquarto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registralimpeza`
--

DROP TABLE IF EXISTS `registralimpeza`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registralimpeza` (
  `id` int NOT NULL AUTO_INCREMENT,
  `numquarto` int DEFAULT NULL,
  `horaEntrada` timestamp(3) NULL DEFAULT NULL,
  `tempoTotal` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12321 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registralocado`
--

DROP TABLE IF EXISTS `registralocado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registralocado` (
  `idlocacao` int NOT NULL AUTO_INCREMENT,
  `numquarto` int NOT NULL,
  `horainicio` timestamp(3) NULL DEFAULT NULL,
  `horafim` timestamp(3) NULL DEFAULT NULL,
  `numpessoas` int DEFAULT NULL,
  `valorquarto` float DEFAULT NULL,
  `valorconsumo` float DEFAULT NULL,
  `pagodinheiro` float DEFAULT NULL,
  `pagopix` float DEFAULT NULL,
  `pagocartao` float DEFAULT NULL,
  `idcaixaatual` int DEFAULT NULL,
  `videolink` char(250) DEFAULT NULL,
  `periodo_locado` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`idlocacao`),
  KEY `numquarto` (`numquarto`)
) ENGINE=InnoDB AUTO_INCREMENT=12159 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registramanutencao`
--

DROP TABLE IF EXISTS `registramanutencao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registramanutencao` (
  `id` int NOT NULL AUTO_INCREMENT,
  `numquarto` int NOT NULL,
  `horaEntrada` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tempoTotal` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registrareserva`
--

DROP TABLE IF EXISTS `registrareserva`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registrareserva` (
  `id` int NOT NULL AUTO_INCREMENT,
  `numquarto` int NOT NULL,
  `horaEntrada` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tempoTotal` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=787 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registravendido`
--

DROP TABLE IF EXISTS `registravendido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registravendido` (
  `idlocacao` int NOT NULL,
  `idproduto` int NOT NULL,
  `quantidade` int NOT NULL,
  `valorunidade` float NOT NULL,
  `valortotal` float NOT NULL,
  `idcaixaatual` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `fk_idlocacao` (`idlocacao`)
) ENGINE=InnoDB AUTO_INCREMENT=7855 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reservas`
--

DROP TABLE IF EXISTS `reservas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservas` (
  `id_reserva` int NOT NULL AUTO_INCREMENT,
  `numero_quarto` int NOT NULL,
  `data_entrada` date NOT NULL,
  `horario_entrada` time NOT NULL,
  `tempo_permanencia` enum('periodo','pernoite') COLLATE utf8mb4_unicode_ci NOT NULL,
  `valor_pago` decimal(10,2) NOT NULL,
  `observacao` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `criado_em` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_reserva`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `status`
--

DROP TABLE IF EXISTS `status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `status` (
  `numeroquarto` int NOT NULL,
  `atualquarto` varchar(45) NOT NULL,
  `horastatus` timestamp(3) NULL DEFAULT NULL,
  `periodo` varchar(45) NOT NULL,
  `adicional` float NOT NULL,
  PRIMARY KEY (`numeroquarto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `valorcartao`
--

DROP TABLE IF EXISTS `valorcartao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `valorcartao` (
  `id` int NOT NULL AUTO_INCREMENT,
  `idlocacao` int NOT NULL,
  `valorcredito` decimal(10,2) DEFAULT '0.00',
  `valordebito` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `idlocacao` (`idlocacao`),
  CONSTRAINT `valorcartao_ibfk_1` FOREIGN KEY (`idlocacao`) REFERENCES `registralocado` (`idlocacao`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2999 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-08 16:47:07
