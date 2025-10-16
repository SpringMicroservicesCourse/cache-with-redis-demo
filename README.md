# cache-with-redis-demo

> Spring Cache abstraction with Redis backend for distributed caching

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Cache](https://img.shields.io/badge/Spring%20Cache-Abstraction-blue.svg)](https://spring.io/guides/gs/caching/)
[![Redis](https://img.shields.io/badge/Redis-latest-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive demonstration of **Spring Cache abstraction with Redis** as cache provider, featuring declarative caching with `@Cacheable`, automatic TTL expiration, and distributed cache management for cluster deployments.

## Features

- Spring Cache abstraction (declarative caching)
- Redis as distributed cache backend
- `@EnableCaching` to enable cache support
- `@Cacheable` for method result caching
- `@CacheEvict` for cache invalidation
- `@CacheConfig` for class-level cache configuration
- Automatic TTL expiration (5 seconds demo)
- Spring Data Redis integration
- RedisCacheManager auto-configuration
- Cache hit/miss observation via SQL logging
- Suitable for multi-node cluster deployments
- Cache consistency across all nodes
- Actuator metrics for cache monitoring

## Tech Stack

- Spring Boot 3.4.5
- Spring Cache (abstraction layer)
- Spring Data Redis (Redis integration)
- Spring Boot Actuator (metrics)
- Redis (distributed cache)
- Spring Data JPA
- Java 21
- H2 Database 2.3.232
- Joda Money 2.0.2
- Lombok
- Maven 3.8+

## Getting Started

### Prerequisites

- JDK 21 or higher
- Maven 3.8+ (or use included Maven Wrapper)
- Docker (for Redis)

### Quick Start

**Step 1: Start Redis**

```bash
# Using Docker Compose (recommended)
docker-compose up -d

# Or using docker run
docker run -d \
  --name redis-spring-course \
  -p 6379:6379 \
  redis
```

**Step 2: Verify Redis**

```bash
# Test Redis connection
docker exec -it redis-spring-course redis-cli ping
# Expected: PONG
```

**Step 3: Run the application**

```bash
./mvnw spring-boot:run
```

## Configuration

### Application Properties

```properties
# JPA/Hibernate configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.show_sql=true
spring.jpa.properties.hibernate.format_sql=true

# Actuator endpoints (for metrics)
management.endpoints.web.exposure.include=*

# Spring Cache configuration
spring.cache.type=redis                        # Use Redis as cache provider
spring.cache.cache-names=coffee                # Pre-create cache named "coffee"
spring.cache.redis.time-to-live=5000           # TTL: 5 seconds (5000ms)
spring.cache.redis.cache-null-values=false     # Don't cache null values

# Redis connection configuration
spring.redis.host=localhost
```

**Configuration Explanation:**

- **spring.cache.type=redis**:
  - Specifies Redis as cache implementation
  - Spring Boot auto-configures `RedisCacheManager`
  
- **spring.cache.cache-names=coffee**:
  - Pre-creates cache named "coffee"
  - Can configure multiple: `coffee,menu,order`
  
- **spring.cache.redis.time-to-live=5000**:
  - Sets cache TTL to 5 seconds (milliseconds)
  - Cache automatically expires after 5 seconds
  
- **spring.cache.redis.cache-null-values=false**:
  - Don't cache null values (prevents cache penetration)

### Docker Compose

**docker-compose.yml:**

```yaml
services:
  redis-spring-course:
    image: redis
    container_name: redis-spring-course
    ports:
      - "6379:6379"
    volumes:
      - ./redis-data:/data
```

## Usage

### Application Flow

```
1. Spring Boot starts
   ↓
2. @EnableCaching activates cache support
   ↓
3. RedisCacheManager auto-configured
   ↓
4. H2 database initialized with schema.sql
   - Creates t_coffee table
   - Inserts 5 coffee records
   ↓
5. ApplicationRunner executes:
   - First call to findAllCoffee() → SQL executed, result cached to Redis (TTL=5s)
   - Next 5 calls (within 5s) → Cache hit, no SQL
   - Sleep 5 seconds → Cache expires
   - Next call → SQL executed again, result cached
```

### Code Example

```java
@Slf4j
@SpringBootApplication
@EnableJpaRepositories
@EnableCaching(proxyTargetClass = true)  // Enable caching!
public class SpringBucksApplication implements ApplicationRunner {
    
    @Autowired
    private CoffeeService coffeeService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // First call: Executes SQL, caches result (TTL=5s)
        log.info("Count: {}", coffeeService.findAllCoffee().size());
        
        // Next 5 calls: Cache hit, no SQL
        for (int i = 0; i < 5; i++) {
            log.info("Reading from cache.");
            coffeeService.findAllCoffee();
        }
        
        // Wait for TTL expiration
        Thread.sleep(5_000);
        
        // After TTL expired: Executes SQL again
        log.info("Reading after refresh.");
        coffeeService.findAllCoffee().forEach(c -> log.info("Coffee {}", c.getName()));
    }
}
```

### Sample Output

```
Hibernate: 
    select
        c1_0.id,
        c1_0.create_time,
        c1_0.name,
        c1_0.price,
        c1_0.update_time 
    from
        t_coffee c1_0

Count: 5
Reading from cache.
Reading from cache.
Reading from cache.
Reading from cache.
Reading from cache.

# (5 seconds pass - cache expires)

Hibernate: 
    select
        c1_0.id,
        c1_0.create_time,
        c1_0.name,
        c1_0.price,
        c1_0.update_time 
    from
        t_coffee c1_0

Reading after refresh.
Coffee espresso
Coffee latte
Coffee capuccino
Coffee mocha
Coffee macchiato
```

**Output Analysis:**
- **First SQL**: Initial query, result cached to Redis (TTL=5s)
- **5 cache hits**: No SQL statements (reading from Redis cache)
- **5 seconds sleep**: Waiting for TTL expiration
- **Second SQL**: After TTL expired, cache invalidated, query executes again

## Key Components

### CoffeeService (with Caching)

```java
@Slf4j
@Service
@CacheConfig(cacheNames = "coffee")  // Class-level cache configuration
public class CoffeeService {
    
    @Autowired
    private CoffeeRepository coffeeRepository;
    
    /**
     * Find all coffees from database
     * @Cacheable: Result will be cached to Redis
     * - First call: Executes method, caches result with TTL
     * - Subsequent calls (before TTL): Returns cached result, method not executed
     * - After TTL expires: Cache invalidated, method executes again
     */
    @Cacheable
    public List<Coffee> findAllCoffee() {
        return coffeeRepository.findAll();
    }
    
    /**
     * Reload coffee data
     * @CacheEvict: Clears the cache
     * - Next call to findAllCoffee() will execute query again
     */
    @CacheEvict
    public void reloadCoffee() {
        // Method body can be empty
        // Calling this method triggers cache eviction
    }
    
    /**
     * Find coffee by name (NOT cached)
     */
    public Optional<Coffee> findOneCoffee(String name) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("name", exact().ignoreCase());
        Optional<Coffee> coffee = coffeeRepository.findOne(
                Example.of(Coffee.builder().name(name).build(), matcher));
        log.info("Coffee Found: {}", coffee);
        return coffee;
    }
}
```

**Annotation Explanation:**

- **@CacheConfig(cacheNames = "coffee")**:
  - Defines cache name at class level
  - All cache annotations in this class inherit this configuration
  
- **@Cacheable**:
  - No `key` specified → Uses method parameters as key
  - `findAllCoffee()` has no parameters → Default key is `SimpleKey.EMPTY`
  - Cache check flow:
    1. Check if cache exists for key in Redis
    2. If exists and not expired: Return cached value (method not executed)
    3. If not exists or expired: Execute method, cache result with TTL
  
- **@CacheEvict**:
  - Clears all entries in "coffee" cache from Redis
  - Executed after method completes (default behavior)

### Main Application

```java
@Slf4j
@EnableTransactionManagement
@SpringBootApplication
@EnableJpaRepositories
@EnableCaching(proxyTargetClass = true)  // MUST enable caching!
public class SpringBucksApplication implements ApplicationRunner {
    
    @Autowired
    private CoffeeService coffeeService;
    
    public static void main(String[] args) {
        SpringApplication.run(SpringBucksApplication.class, args);
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Count: {}", coffeeService.findAllCoffee().size());
        for (int i = 0; i < 5; i++) {
            log.info("Reading from cache.");
            coffeeService.findAllCoffee();
        }
        Thread.sleep(5_000);  // Wait for TTL expiration
        log.info("Reading after refresh.");
        coffeeService.findAllCoffee().forEach(c -> log.info("Coffee {}", c.getName()));
    }
}
```

**Important:**
- `@EnableCaching` is **required** - without it, cache annotations won't work
- `proxyTargetClass = true` uses CGLIB proxying (recommended)

### Database Schema

**schema.sql:**

```sql
drop table t_coffee if exists;
drop table t_order if exists;
drop table t_order_coffee if exists;

create table t_coffee (
    id bigint auto_increment,
    create_time timestamp,
    update_time timestamp,
    name varchar(255),
    price bigint,              -- Stored in cents
    primary key (id)
);

create table t_order (
    id bigint auto_increment,
    create_time timestamp,
    update_time timestamp,
    customer varchar(255),
    state integer not null,
    primary key (id)
);

create table t_order_coffee (
    coffee_order_id bigint not null,
    items_id bigint not null
);

-- Initial data (5 coffees)
insert into t_coffee (name, price, create_time, update_time) 
    values ('espresso', 10000, now(), now());
insert into t_coffee (name, price, create_time, update_time) 
    values ('latte', 12500, now(), now());
insert into t_coffee (name, price, create_time, update_time) 
    values ('capuccino', 12500, now(), now());
insert into t_coffee (name, price, create_time, update_time) 
    values ('mocha', 15000, now(), now());
insert into t_coffee (name, price, create_time, update_time) 
    values ('macchiato', 15000, now(), now());
```

## Redis Cache Verification

### Verify Cache Data

```bash
# Connect to Redis
docker exec -it redis-spring-course redis-cli

# View all keys
127.0.0.1:6379> KEYS *
1) "coffee::SimpleKey []"

# Check key type
127.0.0.1:6379> TYPE "coffee::SimpleKey []"
string

# View cache value (serialized, not human-readable)
127.0.0.1:6379> GET "coffee::SimpleKey []"
"\xac\xed\x00\x05sr\x00\x13java.util.ArrayList..."

# Check TTL (remaining time)
127.0.0.1:6379> TTL "coffee::SimpleKey []"
(integer) 3  # 3 seconds remaining

# Wait 5 seconds, then check again
127.0.0.1:6379> KEYS *
(empty array)  # Cache expired and deleted

# Exit
127.0.0.1:6379> exit
```

### Cache Key Format

**Format:** `{cacheName}::{key}`

**Example:** `coffee::SimpleKey []`
- `coffee`: Cache name (from `@CacheConfig(cacheNames = "coffee")`)
- `::`: Separator
- `SimpleKey []`: Cache key (no method parameters, uses `SimpleKey.EMPTY`)

### Redis Monitoring

**Real-time Monitor:**

```bash
# Monitor all Redis commands
docker exec -it redis-spring-course redis-cli MONITOR

# After starting Spring Boot app, you'll see:
# 1697012345.123456 [0 127.0.0.1:12345] "GET" "coffee::SimpleKey []"
# 1697012345.234567 [0 127.0.0.1:12345] "SETEX" "coffee::SimpleKey []" "5" "\xac\xed..."
# 1697012345.345678 [0 127.0.0.1:12345] "GET" "coffee::SimpleKey []"
# ... (5 GET commands, all hit cache)
```

**Observation Points:**
- First call: `GET` miss → `SETEX` writes to cache with TTL
- Next 5 calls: `GET` hits cache
- After 5 seconds: Cache expires, next `GET` misses → `SETEX` again

## Spring Cache Annotations

### @EnableCaching

**Purpose:** Enable Spring Cache support

```java
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
public class SpringBucksApplication {
    // ...
}
```

**Parameters:**
- `proxyTargetClass = true`: Use CGLIB proxy (default is JDK dynamic proxy)
- **MUST add this annotation**, otherwise cache annotations won't work

**How It Works:**
- Spring uses **AOP (Aspect-Oriented Programming)** to implement caching
- Intercepts methods annotated with cache annotations
- Adds cache logic before and after method execution

### @Cacheable

**Purpose:** Cache method result

```java
@Cacheable(
    cacheNames = "coffee",           // Cache name (required)
    key = "#id",                     // Cache key (SpEL expression)
    condition = "#id > 0",           // Cache condition
    unless = "#result == null"       // Exclusion condition (don't cache if null)
)
public Coffee findCoffee(Long id) {
    return coffeeRepository.findById(id).orElse(null);
}
```

**Execution Flow:**
1. Check if cache exists for key in Redis
2. **Exists**: Return cached value, don't execute method
3. **Not exists**: Execute method, cache result with TTL, return value

**SpEL Expressions:**
- `#id`: Method parameter `id`
- `#result`: Method return value
- `#root.methodName`: Method name
- `#root.target`: Target object

### @CacheEvict

**Purpose:** Clear cache

```java
@CacheEvict(
    cacheNames = "coffee",
    key = "#id",                     // Clear specific key
    allEntries = true,               // Clear all entries
    beforeInvocation = false         // Evict after method execution (default)
)
public void deleteCoffee(Long id) {
    coffeeRepository.deleteById(id);
}
```

**Use Cases:**
- After data update → Clear cache
- Scheduled cache refresh
- Manual cache invalidation

### @CachePut

**Purpose:** Force cache update (always execute method)

```java
@CachePut(cacheNames = "coffee", key = "#coffee.id")
public Coffee updateCoffee(Coffee coffee) {
    return coffeeRepository.save(coffee);
}
```

**Difference from @Cacheable:**
- `@Cacheable`: Cache hit → Don't execute method
- `@CachePut`: Always execute method → Update cache

### @Caching

**Purpose:** Combine multiple cache operations

```java
@Caching(
    cacheable = {
        @Cacheable(cacheNames = "coffee", key = "#id")
    },
    put = {
        @CachePut(cacheNames = "coffee-list", key = "'all'")
    },
    evict = {
        @CacheEvict(cacheNames = "old-coffee", key = "#id")
    }
)
public Coffee complexCacheOperation(Long id) {
    return coffeeRepository.findById(id).orElse(null);
}
```

### @CacheConfig

**Purpose:** Class-level cache configuration

```java
@Service
@CacheConfig(cacheNames = "coffee")  // Class-level configuration
public class CoffeeService {
    
    @Cacheable  // Inherits cacheNames from @CacheConfig
    public List<Coffee> findAllCoffee() {
        return coffeeRepository.findAll();
    }
    
    @CacheEvict  // Inherits cacheNames from @CacheConfig
    public void reloadCoffee() {
    }
}
```

## Cache vs cache-demo

| Feature | cache-demo | cache-with-redis-demo |
|---------|------------|----------------------|
| Cache Provider | ConcurrentHashMap | Redis |
| Distribution | ❌ Single-node | ✅ Multi-node |
| Persistence | ❌ Lost on restart | ✅ Optional persistence |
| TTL Support | ❌ No | ✅ Yes (auto-expiration) |
| External Service | ❌ Not required | ✅ Requires Redis |
| Cache Consistency | ❌ Each node independent | ✅ All nodes share same cache |
| Use Case | Dev/testing | Production cluster |
| Code Changes | None! | None! (Same code) |

**Key Insight:** Code is **identical** between `cache-demo` and `cache-with-redis-demo`! Only configuration changes.

## Monitoring

### Actuator Cache Metrics

**View available cache metrics:**

```bash
# List all cache-related metrics
curl http://localhost:8080/actuator/metrics | jq '.names[] | select(startswith("cache"))'

# Output:
# "cache.gets"        # Cache get requests
# "cache.puts"        # Cache put requests
# "cache.evictions"   # Cache evictions
# "cache.size"        # Cache size
```

### Cache Hit/Miss Statistics

**View cache.gets metric:**

```bash
curl http://localhost:8080/actuator/metrics/cache.gets
```

**Output:**

```json
{
  "name": "cache.gets",
  "description": "The number of pending requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 0
    }
  ],
  "availableTags": [
    {
      "tag": "result",
      "values": ["hit", "pending", "miss"]
    },
    {
      "tag": "cache.manager",
      "values": ["cacheManager"]
    },
    {
      "tag": "cache",
      "values": ["coffee"]
    }
  ]
}
```

**Query hit count:**

```bash
curl http://localhost:8080/actuator/metrics/cache.gets?tag=result:hit&tag=cache:coffee

# Output:
# {
#   "name": "cache.gets",
#   "measurements": [{"statistic": "COUNT", "value": 15.0}]
# }
```

**Query miss count:**

```bash
curl http://localhost:8080/actuator/metrics/cache.gets?tag=result:miss&tag=cache:coffee

# Output:
# {
#   "name": "cache.gets",
#   "measurements": [{"statistic": "COUNT", "value": 3.0}]
# }
```

**Calculate hit rate:**

```bash
# Hit rate = 15 / (15 + 3) × 100% = 83.33%
```

### Cache Statistics Script

**cache-stats.sh:**

```bash
#!/bin/bash
# Cache statistics script

ACTUATOR_URL="http://localhost:8080/actuator/metrics"

get_metric() {
    local metric=$1
    local tags=$2
    curl -s "${ACTUATOR_URL}/${metric}?${tags}" | jq '.measurements[0].value'
}

echo "=== Coffee Cache Statistics ==="

HIT=$(get_metric "cache.gets" "tag=result:hit&tag=cache:coffee")
MISS=$(get_metric "cache.gets" "tag=result:miss&tag=cache:coffee")
PUTS=$(get_metric "cache.puts" "tag=cache:coffee")
EVICTIONS=$(get_metric "cache.evictions" "tag=cache:coffee")

echo "Hits: $HIT"
echo "Misses: $MISS"
echo "Puts: $PUTS"
echo "Evictions: $EVICTIONS"

if [ "$HIT" != "null" ] && [ "$MISS" != "null" ]; then
    TOTAL=$(echo "$HIT + $MISS" | bc)
    if [ "$TOTAL" != "0" ]; then
        HIT_RATE=$(echo "scale=2; $HIT / $TOTAL * 100" | bc)
        echo "Hit Rate: ${HIT_RATE}%"
    fi
fi
```

**Usage:**

```bash
chmod +x cache-stats.sh
./cache-stats.sh
```

**Sample Output:**

```
=== Coffee Cache Statistics ===
Hits: 45
Misses: 5
Puts: 5
Evictions: 0
Hit Rate: 90.00%
```

### Redis Monitoring

**Database Statistics:**

```bash
docker exec -it redis-spring-course redis-cli

127.0.0.1:6379> DBSIZE
(integer) 1

127.0.0.1:6379> INFO keyspace
# Keyspace
db0:keys=1,expires=0,avg_ttl=0
```

**Memory Usage:**

```bash
docker exec -it redis-spring-course redis-cli INFO memory

# Output:
# used_memory_human:1.5M
# used_memory_peak_human:2.0M
# mem_fragmentation_ratio:1.5
```

## Spring Cache Best Practices

### 1. Cacheable Best Practices

```java
// ✅ Recommended: Specify key and unless
@Cacheable(
    cacheNames = "coffee",
    key = "#id",
    unless = "#result == null"  // Don't cache null
)
public Coffee findCoffee(Long id) {
    return coffeeRepository.findById(id).orElse(null);
}

// ✅ Recommended: Use condition to control caching
@Cacheable(
    cacheNames = "coffee",
    key = "#name",
    condition = "#name != null && #name.length() > 0"
)
public Coffee findByName(String name) {
    return coffeeRepository.findByName(name);
}

// ❌ Not recommended: Cache complex parameter methods
@Cacheable(cacheNames = "order")
public List<Order> findOrders(OrderQuery query) {  // Complex object as key
    // May cause long keys or serialization issues
}
```

### 2. CacheEvict Best Practices

```java
// ✅ Recommended: Clear cache after update
@CacheEvict(cacheNames = "coffee", key = "#coffee.id")
public Coffee updateCoffee(Coffee coffee) {
    return coffeeRepository.save(coffee);
}

// ✅ Recommended: Clear multiple related caches
@CacheEvict(cacheNames = {"coffee", "menu"}, allEntries = true)
public void refreshAllCaches() {
}

// ✅ Recommended: Evict before method execution
@CacheEvict(
    cacheNames = "coffee",
    beforeInvocation = true  // Evict before method execution
)
public void deleteCoffee(Long id) {
    coffeeRepository.deleteById(id);
    // If delete fails, cache already cleared, avoids dirty data
}
```

### 3. TTL Configuration

**Test different TTLs:**

```properties
# Test 1: 10 seconds TTL
spring.cache.redis.time-to-live=10000

# Test 2: Never expire (not recommended)
spring.cache.redis.time-to-live=0

# Test 3: 1 hour
spring.cache.redis.time-to-live=3600000

# Test 4: 10 minutes (production)
spring.cache.redis.time-to-live=600000
```

### 4. Advanced Configuration (Programmatic)

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                // Set TTL
                .entryTtl(Duration.ofMinutes(10))
                // Don't cache null values
                .disableCachingNullValues()
                // Set key serialization
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                // Set value serialization (JSON)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
    }
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // Different TTL for different caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // coffee cache: 10 minutes
        cacheConfigurations.put("coffee",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10)));
        
        // menu cache: 30 minutes
        cacheConfigurations.put("menu",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30)));
        
        return RedisCacheManager.builder(factory)
                .cacheDefaults(redisCacheConfiguration())
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
```

## Testing

### Disable Cache for Testing

```properties
# Disable cache in test environment
spring.cache.type=none
```

### Clear Cache in Tests

```java
@SpringBootTest
public class CoffeeServiceTest {
    
    @Autowired
    private CacheManager cacheManager;
    
    @BeforeEach
    public void clearCache() {
        cacheManager.getCacheNames().forEach(name -> 
                cacheManager.getCache(name).clear());
    }
    
    @Test
    public void testFindCoffee() {
        // Test logic
    }
}
```

### Run Tests

```bash
# Run tests
./mvnw test

# Run application
./mvnw spring-boot:run
```

## Common Issues

### Issue 1: Cache Not Working

**Symptoms:** SQL still executes on every call

**Possible Causes:**
1. ❌ Missing `@EnableCaching`
2. ❌ Redis not running
3. ❌ Method is `private` (AOP can't intercept)
4. ❌ Internal call (not through Spring proxy)

**Solutions:**

```java
// ✅ Ensure @EnableCaching
@SpringBootApplication
@EnableCaching
public class Application { }

// ✅ Check Redis status
docker ps | grep redis

// ✅ Method must be public
public List<Coffee> findAllCoffee() { }

// ✅ Call through injected bean
@Autowired
private CoffeeService coffeeService;  // Not this.method()
```

### Issue 2: Redis Connection Failed

**Error:**

```
io.lettuce.core.RedisConnectionException: Unable to connect to localhost:6379
```

**Solutions:**

```bash
# Check if Redis is running
docker ps | grep redis

# Start Redis if not running
docker start redis-spring-course

# Or start new Redis container
docker-compose up -d

# Test connection
docker exec -it redis-spring-course redis-cli ping
```

### Issue 3: Cache Expires Too Fast

**Symptoms:** Every query hits database (low hit rate)

**Cause:** TTL set too short (5 seconds is for demo only)

**Solution:**

```properties
# Production: Use longer TTL
spring.cache.redis.time-to-live=600000  # 10 minutes

# Or disable TTL
spring.cache.redis.time-to-live=0  # Never expire (use with caution)
```

### Issue 4: Serialization Error

**Error:**

```
org.springframework.data.redis.serializer.SerializationException: 
Cannot deserialize
```

**Cause:** Entity class doesn't implement `Serializable`

**Solution:**

```java
// Add Serializable to BaseEntity
@MappedSuperclass
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    // ...
}
```

## Cache Strategy Comparison

### In-Memory vs Distributed Cache

**In-Memory Cache (ConcurrentHashMap):**

```
App A cache: espresso = $1.00
App B cache: espresso = $1.00
Update database: espresso = $1.20
Clear App A cache: espresso reloads as $1.20
App B cache: Still $1.00 (inconsistent!)
```

**Distributed Cache (Redis):**

```
Apps A, B, C all access same Redis
Update database: espresso = $1.20
Clear Redis cache
All apps next access: Get $1.20 (consistent!)
```

### When to Use Redis Cache

**✅ Use Redis Cache:**
- Cluster deployment (multiple nodes)
- Need cache consistency across nodes
- Need to share cache between applications
- Need TTL auto-expiration
- Data changes moderately
- Need cache persistence (optional)

**❌ Use In-Memory Cache:**
- Single-node deployment
- Data rarely changes
- Can tolerate short-term inconsistency
- Small cache data size

## Performance Optimization

### Cache Warm-up

```java
@Component
public class CacheWarmer implements ApplicationRunner {
    
    @Autowired
    private CoffeeService coffeeService;
    
    @Override
    public void run(ApplicationArguments args) {
        // Warm up cache on application startup
        coffeeService.findAllCoffee();
        log.info("Cache warmed up");
    }
}
```

### Two-Level Cache

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Primary
    @Bean
    public CacheManager cacheManager(
            CacheManager caffeineCacheManager,
            CacheManager redisCacheManager) {
        
        // Combine two CacheManagers
        return new CompositeCacheManager(
                caffeineCacheManager,  // L1: In-memory cache (fast)
                redisCacheManager      // L2: Redis cache (distributed)
        );
    }
}
```

### Random TTL (Prevent Cache Avalanche)

```java
@Bean
public RedisCacheConfiguration redisCacheConfiguration() {
    // Base TTL: 10 minutes
    long baseTtl = 600;
    // Random range: ± 60 seconds
    long randomTtl = ThreadLocalRandom.current().nextLong(-60, 60);
    
    return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(baseTtl + randomTtl));
}
```

## Best Practices Demonstrated

1. **Declarative Caching**: Using annotations instead of programmatic caching
2. **@CacheConfig**: Class-level cache configuration
3. **Automatic TTL**: Redis auto-expires cache entries
4. **Distributed Cache**: Cache consistency across cluster nodes
5. **Actuator Metrics**: Monitor cache hit rate and performance
6. **Cache Aside Pattern**: Standard caching strategy
7. **SQL Logging**: Verify cache hit/miss through SQL statements

## Security Configuration

### Password Authentication

**docker-compose.yml:**

```yaml
services:
  redis-spring-course:
    image: redis
    container_name: redis-spring-course
    ports:
      - "127.0.0.1:6379:6379"  # Bind to localhost only
    volumes:
      - ./redis-data:/data
    command: redis-server --appendonly yes --requirepass your-password
```

**application.properties:**

```properties
spring.redis.host=localhost
spring.redis.password=your-password
```

## References

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Spring Boot Caching](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.caching)
- [Spring Data Redis](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Redis Documentation](https://redis.io/docs/)
- [Spring Cache Annotations](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-annotations)
- [Micrometer Cache Metrics](https://micrometer.io/docs/concepts#_caches)

## License

MIT License - see [LICENSE](LICENSE) file for details.

## About Us

我們主要專注在敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。喜歡把先進技術和實務經驗結合，打造好用又靈活的軟體解決方案。近來也積極結合 AI 技術,推動自動化工作流，讓開發與運維更有效率、更智慧。持續學習與分享，希望能一起推動軟體開發的創新和進步。

## Contact

**風清雲談** - 專注於敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。

- 🌐 官方網站：[風清雲談部落格](https://blog.fengqing.tw/)
- 📘 Facebook：[風清雲談粉絲頁](https://www.facebook.com/profile.php?id=61576838896062)
- 💼 LinkedIn：[Chu Kuo-Lung](https://www.linkedin.com/in/chu-kuo-lung)
- 📺 YouTube：[雲談風清頻道](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- 📧 Email：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**⭐ 如果這個專案對您有幫助，歡迎給個 Star！**
