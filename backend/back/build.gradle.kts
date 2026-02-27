plugins {
    // 1. Kotlin 관련 플러그인 추가
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0" // Spring의 final 클래스 문제를 해결 (all-open)
    kotlin("plugin.jpa") version "2.1.0"    // JPA Entity의 기본 생성자 문제를 해결 (no-arg)

    // 2. Querydsl을 위한 KSP (Annotation Processor 대체)
    kotlin("kapt") version "2.1.0"

    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.1.0"
    id("checkstyle")
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "back"
val querydslVersion = "6.10.1"

kapt {
    keepJavacAnnotationProcessors = true // 자바 어노테이션 프로세서(Lombok 등)와 함께 사용
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

spotless {
    java {
        target("src/**/*.java")

        palantirJavaFormat()   // 기본 포맷팅
        removeUnusedImports()  // 안 쓰는 import 제거
        trimTrailingWhitespace() // 줄 끝 공백 제거
        formatAnnotations()    // @Test, @Override 같은 어노테이션 배치 최적화
        endWithNewline()       // 모든 파일의 끝에 빈 줄 하나를 추가 (POSIX 표준 준수
        // import 구문을 알파벳 순서나 특정 규칙대로 정렬 (코드 리뷰 시 편함)
        importOrder(
            "java",
            "javax",
            "org",
            "com",
            ""
        )
    }

    // Java 외 파일  정렬
    format("misc") {
        target("*.gradle", "*.md", ".gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("$rootDir/config/checkstyle/checkstyle.xml")
}



dependencies {

    // 6. Kotlin 필수 라이브러리
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    //kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    //java lombok
    annotationProcessor("org.projectlombok:lombok")

    //kotlin lombok
    compileOnly("org.projectlombok:lombok:1.18.34")
    kapt("org.projectlombok:lombok:1.18.34")

    // 테스트에서도 롬복이 필요하다면
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    kaptTest("org.projectlombok:lombok:1.18.34")

    // Social Login
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // Querydsl
    implementation("io.github.openfeign.querydsl:querydsl-core:$querydslVersion")
    implementation("io.github.openfeign.querydsl:querydsl-jpa:$querydslVersion")
    kapt("io.github.openfeign.querydsl:querydsl-apt:$querydslVersion:jpa")
    kapt("jakarta.persistence:jakarta.persistence-api")
    kapt("jakarta.annotation:jakarta.annotation-api")
//    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:$querydslVersion:jpa")
//    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
//    annotationProcessor("jakarta.annotation:jakarta.annotation-api")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // DB
    runtimeOnly("com.h2database:h2")
    implementation("org.springframework.boot:spring-boot-h2console")

    // API Docs
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")

    // Crawling
    implementation("org.jsoup:jsoup:1.17.2")

    // Dev tools

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
    implementation("org.jsoup:jsoup:1.17.2") // 웹 크롤링을 위해 Jsoup 라이브러리 추가
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Elasticsearch (Java API Client + Low Level Rest Client)
    // 버전은 반드시 맞춰서 사용하세요.
    implementation("co.elastic.clients:elasticsearch-java:8.11.3")
    implementation("org.elasticsearch.client:elasticsearch-rest-client:8.11.3")
    implementation("org.apache.httpcomponents.client5:httpclient5")

    // prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    //mysql
    runtimeOnly ("com.mysql:mysql-connector-j")

}

tasks.withType<Test> {
    useJUnitPlatform()
    // CI 환경(보통 2코어)과 로컬 환경에 맞춰 동적으로 코어 할당
    maxParallelForks = 2

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
