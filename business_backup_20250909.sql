-- MySQL dump 10.13  Distrib 9.3.0, for macos14.7 (arm64)
--
-- Host: 54.180.232.45    Database: TogetherShop
-- ------------------------------------------------------
-- Server version	8.0.43-0ubuntu0.24.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `business`
--

DROP TABLE IF EXISTS `business`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business` (
  `business_id` bigint NOT NULL AUTO_INCREMENT COMMENT '사업자 고유 식별자',
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `business_name` varchar(200) NOT NULL,
  `business_registration_number` varchar(20) NOT NULL,
  `representative_name` varchar(100) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `business_type` varchar(100) DEFAULT NULL,
  `business_category` varchar(100) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `business_hours` text,
  `description` text,
  `profile_image_url` varchar(500) DEFAULT NULL,
  `verification_status` enum('PENDING','VERIFIED','REJECTED') DEFAULT NULL,
  `together_index` decimal(4,1) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','SUSPENDED') DEFAULT NULL,
  `collaboration_category` varchar(255) DEFAULT NULL,
  `business_fcm_token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`business_id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business`
--

LOCK TABLES `business` WRITE;
/*!40000 ALTER TABLE `business` DISABLE KEYS */;
INSERT INTO `business` VALUES (1,'bizuser1','biz1@example.com','hashedpw1','Alpha Store','123-45-67890','Kim Alpha','010-1234-5678','개인','카페','Seoul, Gangnam-gu, Teheran-ro 123',37.49960000,127.03650000,'09:00-21:00','A cozy coffee shop.',NULL,'VERIFIED',75.5,'2025-09-04 13:59:50','2025-09-04 13:59:50','ACTIVE',NULL,NULL),(2,'bizuser2','biz2@example.com','hashedpw2','Beta Mart','234-56-78901','Lee Beta','010-2345-6789','법인','마트','Seoul, Mapo-gu, World Cup-ro 45',37.56650000,126.90120000,'08:00-22:00','24/7 convenience store.',NULL,'VERIFIED',80.0,'2025-09-04 13:59:50','2025-09-04 13:59:50','ACTIVE',NULL,NULL),(3,'a','a','$2a$10$HZotKghjySLeOVZSQtTZauEYp04X8PiBETuPUugeY7idRB.5OxiGK','a','1234',NULL,NULL,'a','a',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-05 21:20:00','2025-09-05 21:20:00','ACTIVE','a',NULL),(4,'ab','ab','$2a$10$j8pjWBn7cZSZrcVWTptXl.O4Iz.GqsTgDLh4atvV6YVS3ahfWZR4S','a','a',NULL,NULL,'a','a',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-05 21:21:45','2025-09-05 21:21:45','ACTIVE','a',NULL),(5,'abc','abc','$2a$10$KdBB0r4/rPND30/7wGvxBOEE9CXT0qtij7mzEYJbCQ2J5R9gsCh62','abc','abc',NULL,NULL,'abc','abc',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-05 21:23:13','2025-09-05 21:23:13','ACTIVE','abc',NULL),(6,'user1','user1','$2a$10$xGxuejARdgErJwpKTzQ6w.I4.sTf7OtX2QV.UKQY7ZHTWW4h0Yxgm','가게1','1234',NULL,NULL,'카페','20대',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-05 21:25:11','2025-09-05 21:25:11','ACTIVE','베이커리',NULL),(7,'asdfa','asdf@naver.com','$2a$10$ApXW1w2e8FSS73ymdm.p8OCenOUHPSJWb.BkK4JzCLx.F6iIMi9Qq','asdf','1234',NULL,NULL,'카페','서비스',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',65.0,'2025-09-06 21:44:39','2025-09-06 21:44:39','ACTIVE','10대',NULL),(8,'user123','user1@naver.com','$2a$10$5v/JfR0s/5dkypToti.1NeRMBJl1CSXBdLiesHnuNaO7ofUnae.dK','함께가게','12341234',NULL,NULL,'음식점','서비스',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',75.0,'2025-09-07 17:37:42','2025-09-07 17:37:42','ACTIVE','20대',NULL),(9,'user234','user234@naver.com','$2a$10$2DfECJdD6w3qxI5XXE6SnO6FgBCqXP6iU.h0pjieOmJaJOttY4RH2','asdf','1234',NULL,NULL,'카페','제조',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-07 19:19:57','2025-09-07 19:19:57','ACTIVE','10대',NULL),(10,'test_biz','test@gmail.com','$2a$10$FiT9XGyZa/tiTG5vdFobHejeOGKGSgeytF6ChA4pWUEkukjA6wDw2','태현식품','123-45-78446',NULL,NULL,'카페','서비스',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-08 02:52:09','2025-09-08 02:52:09','ACTIVE','20대',NULL),(11,'test123','testtest@maver.com','$2a$10$ODwwLO3pRl9ScDY49E4pPu2tsW.EaTdneyMf7UBE.5qrnzckwutDW','test','12123123123123',NULL,NULL,'음식점','서비스',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-08 05:54:57','2025-09-08 05:54:57','ACTIVE','10대',NULL),(12,'user7','user7@naver.com','$2a$10$J00EVnK5XZDJ3bEVqo0sge.uw0r2qRxrYTKol7WQsJt7Lgjhl8gh.','a','a',NULL,NULL,'카페','판매',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',0.0,'2025-09-08 14:12:41','2025-09-08 14:12:41','ACTIVE','10대',NULL),(13,'cafe_moca','moca@example.com','$2a$10$rdU9NOLzB5FJyS9kWNQ0RO0kLz/KFWuoWGskh6LQCYa.akaBBwyMa','카페 모카','123-45-67890','김모카','02-1234-5678','카페','음료','서울시 강남구 테헤란로 123',NULL,NULL,NULL,NULL,NULL,'VERIFIED',NULL,'2024-01-15 10:00:00',NULL,'ACTIVE',NULL,NULL),(14,'pizza_heaven','pizza@example.com','hash456','피자 천국','234-56-78901','이피자','02-2345-6789','레스토랑','음식','서울시 강남구 역삼동 456',NULL,NULL,NULL,NULL,NULL,'VERIFIED',NULL,'2024-01-20 11:00:00',NULL,'ACTIVE',NULL,NULL),(15,'book_store','book@example.com','hash789','북스토어','345-67-89012','박도서','02-3456-7890','서점','도서','서울시 서초구 서초동 789',NULL,NULL,NULL,NULL,NULL,'VERIFIED',NULL,'2024-02-01 09:00:00',NULL,'ACTIVE',NULL,NULL),(16,'user8','user8@naver.com','$2a$10$J56PIrrSpUA9vNo5.vOfbOPpCxy/Sj8ALB0ufguiwBlILhxAU1j1W','스타벅스','123',NULL,NULL,'편의점','제조',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-08 20:49:07','2025-09-08 20:49:07','ACTIVE','10대',NULL),(17,'rnqhs01','bizuser2@example.com','$2a$10$OK3uBsw7BJu9mdJsoVlHPOcTQKyfWLIVgQqMtG8fKxcm5dZCQWUOq','테스트 카페','123-45-67890','구','010-2345-6789','음식점업','일반고객','충남 주공로 주공로 13',NULL,NULL,NULL,NULL,NULL,'PENDING',99.0,'2025-09-09 00:35:02','2025-09-09 00:35:02','ACTIVE','서비스업',NULL),(18,'user9','user9@naver.com','$2a$10$YkrTA1VTDJ..EmxBrxgzBOi26F74ClS6Vt7Y178WM3l.XJVS0JEUq','a','12',NULL,NULL,'카페','제조',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-09 08:27:37','2025-09-09 08:27:37','ACTIVE','20대',NULL),(19,'user10','user10@naver.com','$2a$10$iGbxjcJq1W.gU9zlMKBWse/C/RZIffz0w/1IjAsaWY.ekhsCNLg8W','커피빈','1234',NULL,NULL,'편의점','제조',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-09 13:29:54','2025-09-09 13:29:54','ACTIVE','20대',NULL),(20,'user11','user11@naver.com','$2a$10$L33i/XPWn6si/sevcPPUTuJuJ/QemhnMdhp7.rZ2dc.2Org/Voljm','맥도날드','1234',NULL,NULL,'음식점','제조',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-09 13:41:33','2025-09-09 13:41:33','ACTIVE','10대',NULL),(21,'user12','user12@naver.com','$2a$10$dMYW1CBWqPuX.XvJi13X5OcG48nAZKOfBXY8SOirDEeUY.m25VmOO','버거킹','1234',NULL,NULL,'카페','서비스',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-09 14:42:07','2025-09-09 14:42:07','ACTIVE','20대',NULL),(22,'baribari','baribari@naver.com','$2a$10$.8B1rzPlxRW95B35v3o25e.oJVjc.m.FRYDl3WWJlTv6SSnfU6nSq','바리바리스타','010-22-22222',NULL,NULL,'카페','서비스',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-09 15:36:12','2025-09-09 15:36:12','ACTIVE','10대',NULL),(23,'user13','user13@gmail.com','$2a$10$KAgq6ivNz/.sR97aoQmrceRSJrl/sytJnhVVsJUZ78Qj2Y0jMNUve','도미노','1234',NULL,NULL,'음식점','서비스',NULL,NULL,NULL,NULL,NULL,NULL,'PENDING',NULL,'2025-09-09 15:51:07','2025-09-09 15:51:07','ACTIVE','10대',NULL),(44,'gju01','gju01@example.com','암호화된비번','한마 국수집','214-86-10001','김상민','031-610-1001','음식점','한식','경기 광주시 탄벌동 중앙로 12',37.40980000,127.25540000,'매일 11:00-22:00','국수 전문점',NULL,'VERIFIED',82.5,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Food','fcm_gju01'),(45,'gju02','gju02@example.com','암호화된비번','오포 카페 로스터스','214-86-10002','박지수','031-610-1002','카페','베이커리','경기 광주시 오포읍 오포로 215',37.41450000,127.27500000,'주중 09:00-21:00 / 주말 10:00-22:00','스페셜티 카페',NULL,'PENDING',74.0,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Cafe','fcm_gju02'),(46,'gju03','gju03@example.com','암호화된비번','경춘면옥','214-86-10003','최해린','031-610-1003','음식점','국수','경기 광주시 곤지암읍 곤지암로 77',37.41010000,127.28760000,'매일 10:30-21:30','평양냉면 전문',NULL,'VERIFIED',88.0,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Food','fcm_gju03'),(47,'gju04','gju04@example.com','암호화된비번','초월 베이커리','214-86-10004','이유진','031-610-1004','소매업','빵집','경기 광주시 초월읍 초월로 33',37.41990000,127.26880000,'매일 08:00-20:00','천연발효빵',NULL,'VERIFIED',79.5,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Bakery','fcm_gju04'),(48,'gju05','gju05@example.com','암호화된비번','역동 헬스짐','214-86-10005','정태호','031-610-1005','서비스업','피트니스','경기 광주시 역동 문화로 9',37.41570000,127.25990000,'평일 06:00-23:00 / 주말 08:00-20:00','PT, 필라테스',NULL,'VERIFIED',86.3,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Fitness','fcm_gju05'),(49,'gju06','gju06@example.com','암호화된비번','송정동 마켓','214-86-10006','고유정','031-610-1006','소매업','마트','경기 광주시 송정동 송정로 45',37.41700000,127.29320000,'매일 09:00-23:00','지역 농산물 판매',NULL,'PENDING',71.2,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Retail','fcm_gju06'),(50,'gju07','gju07@example.com','암호화된비번','퇴촌 꽃집','214-86-10007','유라라','031-610-1007','소매업','꽃집','경기 광주시 퇴촌면 퇴촌로 101',37.41230000,127.25250000,'월-토 09:00-19:00','부케 및 행사 꽃',NULL,'VERIFIED',76.8,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Flower','fcm_gju07'),(51,'gju08','gju08@example.com','암호화된비번','오포 동물병원','214-86-10008','문성진','031-610-1008','서비스업','동물병원','경기 광주시 오포읍 오포안로 12',37.41350000,127.26660000,'월-토 10:00-19:00 / 일 12:00-17:00','반려동물 진료',NULL,'VERIFIED',84.1,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Vet','fcm_gju08'),(52,'gju09','gju09@example.com','암호화된비번','광주시 책방','214-86-10009','한도윤','031-610-1009','소매업','서점','경기 광주시 경안동 중앙길 7',37.41880000,127.26120000,'화-일 11:00-20:00','독립서점',NULL,'REJECTED',58.4,'2025-09-09 10:32:06','2025-09-09 10:32:06','INACTIVE','Book','fcm_gju09'),(53,'gju10','gju10@example.com','암호화된비번','곤지암 스테이크하우스','214-86-10010','오세훈','031-610-1010','음식점','양식','경기 광주시 곤지암읍 백마로 222',37.41120000,127.28000000,'매일 11:30-22:00','스테이크 전문',NULL,'VERIFIED',90.0,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Food','fcm_gju10'),(54,'gmg01','gmg01@example.com','암호화된비번','광명동 비스트로','214-86-20001','강민정','031-260-2001','음식점','퓨전','경기 광명시 광명동 광명로 101',37.47650000,126.86650000,'매일 11:00-22:00','파스타와 그릴 요리',NULL,'VERIFIED',85.2,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Food','fcm_gmg01'),(55,'gmg02','gmg02@example.com','암호화된비번','철산 카페 코지','214-86-20002','류지아','031-260-2002','카페','디저트','경기 광명시 철산동 오리로 512',37.47570000,126.87900000,'평일 09:00-21:00 / 주말 10:00-22:00','수제 디저트 카페',NULL,'PENDING',72.9,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Cafe','fcm_gmg02'),(56,'gmg03','gmg03@example.com','암호화된비번','하안동 김밥연구소','214-86-20003','서다윤','031-260-2003','음식점','한식','경기 광명시 하안동 하안로 22',37.46430000,126.87540000,'매일 08:00-21:00','김밥 전문점',NULL,'VERIFIED',80.7,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Food','fcm_gmg03'),(57,'gmg04','gmg04@example.com','암호화된비번','소하 펫케어','214-86-20004','임성우','031-260-2004','서비스업','펫','경기 광명시 소하동 소하로 88',37.44880000,126.87120000,'월-토 10:00-19:00 / 일 12:00-17:00','반려동물 미용/호텔',NULL,'VERIFIED',77.6,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Pet','fcm_gmg04'),(58,'gmg05','gmg05@example.com','암호화된비번','광명 헬스랩','214-86-20005','문태경','031-260-2005','서비스업','피트니스','경기 광명시 광명동 디지털로 23',37.47120000,126.88570000,'평일 06:00-23:00 / 주말 08:00-20:00','헬스센터',NULL,'VERIFIED',89.1,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Fitness','fcm_gmg05'),(59,'gmg06','gmg06@example.com','암호화된비번','일직동 작은도서관','214-86-20006','백하린','031-260-2006','소매업','서점','경기 광명시 일직동 일직로 11',37.43010000,126.86120000,'화-일 11:00-20:00','책방과 문화공간',NULL,'PENDING',63.4,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Culture','fcm_gmg06'),(60,'gmg07','gmg07@example.com','암호화된비번','광명동 마트','214-86-20007','이호연','031-260-2007','소매업','마트','경기 광명시 광명동 중앙로 55',37.47990000,126.86880000,'매일 09:00-23:00','수입 식자재 판매',NULL,'VERIFIED',81.0,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Retail','fcm_gmg07'),(61,'gmg08','gmg08@example.com','암호화된비번','철산 헤어살롱','214-86-20008','한서진','031-260-2008','서비스업','미용','경기 광명시 철산동 철산로 9',37.47000000,126.88120000,'매일 10:00-20:00','미용실',NULL,'REJECTED',55.0,'2025-09-09 10:32:06','2025-09-09 10:32:06','INACTIVE','Beauty','fcm_gmg08'),(62,'gmg09','gmg09@example.com','암호화된비번','소하 마카롱샵','214-86-20009','장보미','031-260-2009','카페','디저트','경기 광명시 소하동 소하안로 3',37.45250000,126.87000000,'평일 10:00-21:00 / 주말 10:00-22:00','마카롱 전문점',NULL,'VERIFIED',78.9,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Cafe','fcm_gmg09'),(63,'gmg10','gmg10@example.com','암호화된비번','광명 정육점','214-86-20010','강준우','031-260-2010','소매업','정육','경기 광명시 광명동 철산로 120',37.46880000,126.87230000,'매일 09:00-21:00','한우 전문 정육점',NULL,'VERIFIED',87.6,'2025-09-09 10:32:06','2025-09-09 10:32:06','ACTIVE','Butcher','fcm_gmg10');
/*!40000 ALTER TABLE `business` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-09 19:46:04
