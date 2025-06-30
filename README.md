# SpringBucks Redis 快取演示專案 ⚡

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-Cache-red.svg)](https://redis.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 專案介紹

SpringBucks 是一個基於 Spring Boot 3.x 的咖啡店管理系統演示專案，主要展示如何在企業級應用中整合 Redis 快取機制。本專案模擬真實的咖啡店業務場景，包含咖啡商品管理、訂單處理等核心功能，並透過 Redis 提供高效能的資料快取服務。

### 核心功能
- **咖啡商品管理**：支援咖啡品項的 CRUD 操作
- **訂單管理系統**：完整的訂單生命週期管理
- **Redis 快取整合**：提升資料查詢效能
- **金額處理**：使用 Joda Money 確保金額計算的精確性
- **資料持久化**：透過 JPA 與 H2 資料庫整合

> 💡 **為什麼選擇此專案？**
> - 展示現代化的 Spring Boot 3.x 架構設計
> - 實際演示 Redis 快取策略的最佳實踐
> - 提供完整的企業級開發範例
> - 適合學習微服務架構中的快取機制

### 🎯 專案特色

- **高效能快取**：使用 Redis 實現多層快取策略，大幅提升查詢效能
- **精確金額處理**：整合 Joda Money 庫，確保財務計算的準確性
- **現代化架構**：採用 Spring Boot 3.4.5 + Java 21 的最新技術棧
- **清晰的程式碼結構**：遵循 DDD 設計模式，易於維護和擴展
- **完整的快取管理**：支援快取失效、更新等完整生命週期管理

## 技術棧

### 核心框架
- **Spring Boot 3.4.5** - 企業級應用開發框架
- **Spring Data JPA** - 資料持久化與 ORM 解決方案
- **Spring Cache** - 宣告式快取抽象層
- **Spring Data Redis** - Redis 整合與操作框架

### 資料儲存與快取
- **Redis** - 高效能記憶體快取資料庫
- **H2 Database** - 內嵌式關聯資料庫（開發用）
- **Hibernate** - JPA 實作與 ORM 框架

### 開發工具與輔助
- **Lombok** - 簡化 Java 程式碼開發
- **Joda Money** - 專業的金額處理函式庫
- **Maven** - 專案構建與依賴管理工具
- **JUnit 5** - 單元測試框架

## 專案結構

```
cache-with-redis-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tw/fengqing/spring/springbucks/
│   │   │       ├── model/              # 實體類別與領域模型
│   │   │       │   ├── BaseEntity.java       # 基礎實體類別
│   │   │       │   ├── Coffee.java           # 咖啡商品實體
│   │   │       │   ├── CoffeeOrder.java      # 訂單實體
│   │   │       │   ├── OrderState.java       # 訂單狀態枚舉
│   │   │       │   └── MoneyConverter.java   # 金額轉換器
│   │   │       ├── repository/         # 資料存取層
│   │   │       │   ├── CoffeeRepository.java
│   │   │       │   └── CoffeeOrderRepository.java
│   │   │       ├── service/            # 業務邏輯層
│   │   │       │   ├── CoffeeService.java     # 咖啡服務（含快取）
│   │   │       │   └── CoffeeOrderService.java
│   │   │       └── SpringBucksApplication.java # 主程式進入點
│   │   └── resources/
│   │       ├── application.properties   # 應用程式配置
│   │       ├── schema.sql              # 資料庫結構定義
│   │       └── data.sql                # 初始資料
│   └── test/
│       └── java/                       # 測試程式碼
├── pom.xml                            # Maven 專案配置
└── README.md                          # 專案說明文件
```

## 快速開始

### 前置需求
- **Java 21** 或更高版本
- **Maven 3.6** 或更高版本
- **Redis Server** （本機執行或 Docker）
- **IDE**：建議使用 IntelliJ IDEA 或 Eclipse

### 安裝與執行

1. **克隆此倉庫：**
```bash
git clone https://github.com/SpringMicroservicesCourse/cache-with-redis-demo.git
```

2. **進入專案目錄：**
```bash
cd cache-with-redis-demo
```

3. **啟動 Redis 服務：**
```bash
# 使用 Docker（推薦）
docker run --name redis-cache -p 6379:6379 -d redis:latest

# 或使用本機安裝的 Redis
redis-server
```

4. **編譯專案：**
```bash
mvn clean compile
```

5. **執行應用程式：**
```bash
mvn spring-boot:run
```

6. **驗證執行狀態：**
   - 應用程式啟動後會自動執行示範程式
   - 觀察控制台輸出，可以看到快取機制的運作情況
   - 首次查詢會從資料庫載入，後續查詢會從 Redis 快取取得

## 進階說明

### 環境變數
```properties
# Redis 連線設定
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-password

# 快取設定
CACHE_TTL=5000
CACHE_NAMES=coffee

# 資料庫設定
DB_URL=jdbc:h2:mem:testdb
```

### 設定檔說明
```properties
# application.properties 主要設定

# JPA 與 Hibernate 設定
spring.jpa.hibernate.ddl-auto=none          # 不自動建立資料表
spring.jpa.properties.hibernate.show_sql=true    # 顯示 SQL 語句
spring.jpa.properties.hibernate.format_sql=true  # 格式化 SQL 輸出

# 監控端點設定
management.endpoints.web.exposure.include=*      # 開放所有監控端點

# Redis 快取設定
spring.cache.type=redis                     # 指定快取類型為 Redis
spring.cache.cache-names=coffee             # 定義快取命名空間
spring.cache.redis.time-to-live=5000        # 快取存活時間（毫秒）
spring.cache.redis.cache-null-values=false  # 不快取 null 值

# Redis 連線設定
spring.redis.host=localhost                 # Redis 伺服器位址
```

### 快取機制說明

本專案在 `CoffeeService` 中實作了兩個重要的快取註解：

```java
// 查詢結果會被快取到 "coffee" 命名空間
@Cacheable("coffee")
public List<Coffee> findAllCoffee() {
    return coffeeRepository.findAll();
}

// 清除 "coffee" 命名空間的所有快取
@CacheEvict("coffee")
public void reloadCoffee() {
}
```

### 金額處理機制

使用自定義的 `MoneyConverter` 來處理 Joda Money 與資料庫之間的轉換：

```java
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, Long> {
    // 將 Money 物件轉換為資料庫的長整數值（以分為單位）
    // 將資料庫值轉換回 Money 物件
}
```

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Data Redis 參考指南](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Redis 官方文件](https://redis.io/documentation)
- [Joda Money 使用指南](https://www.joda.org/joda-money/)

## 注意事項與最佳實踐

### ⚠️ 重要提醒

| 項目 | 說明 | 建議做法 |
|------|------|----------|
| Redis 連線 | 生產環境需要設定密碼 | 使用環境變數管理敏感資訊 |
| 快取策略 | 避免快取過大的物件 | 合理設定 TTL 和記憶體限制 |
| 金額精度 | 使用 Joda Money 避免浮點數誤差 | 統一使用最小貨幣單位儲存 |
| 資料一致性 | 注意快取與資料庫的同步 | 適當使用 @CacheEvict 清除快取 |

### 🔒 最佳實踐指南

- **快取命名**：使用有意義的快取命名空間，避免衝突
- **TTL 設定**：根據業務需求合理設定快取存活時間
- **異常處理**：Redis 連線失敗時應有適當的降級機制
- **監控告警**：監控快取命中率和 Redis 效能指標
- **資料一致性**：在更新資料時及時清除相關快取

### 效能優化建議

1. **快取預熱**：應用啟動時載入熱點資料到快取
2. **分層快取**：結合本地快取和 Redis 快取
3. **批次操作**：使用 Redis Pipeline 提升批次操作效能
4. **連線池**：合理配置 Redis 連線池參數

## 授權說明

本專案採用 MIT 授權條款，詳見 LICENSE 檔案。

## 關於我們

我們主要專注在敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。喜歡把先進技術和實務經驗結合，打造好用又靈活的軟體解決方案。

## 聯繫我們

- **FB 粉絲頁**：[風清雲談 | Facebook](https://www.facebook.com/profile.php?id=61576838896062)
- **LinkedIn**：[linkedin.com/in/chu-kuo-lung](https://www.linkedin.com/in/chu-kuo-lung)
- **YouTube 頻道**：[雲談風清 - YouTube](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- **風清雲談 部落格**：[風清雲談](https://blog.fengqing.tw/)
- **電子郵件**：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**📅 最後更新：2025-06-30**  
**👨‍💻 維護者：風清雲談團隊** 