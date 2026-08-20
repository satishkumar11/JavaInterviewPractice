# Java Interview Practice

A collection of design pattern demos, low-level design implementations, practice problems, and interview prep material.

## Contents

- [Interview Prep Docs](#interview-prep-docs)
- [Interview Questions](#interview-questions)
- [Design Patterns](#design-patterns)
- [Low-Level Design](#low-level-design)
- [Node.js API Design](#nodejs-api-design)
- [Practice Problems](#practice-problems)

## Interview Prep Docs ![Interview Prep Docs](https://img.shields.io/badge/-Reference-blueviolet?style=for-the-badge)

- [Mercans Interview — Master Answer Book](Mercans_Interview_Master_Answer_Book.pdf)
- [Spring & Spring Cloud — Interview Q&A](spring-interview-qa.pdf)
- [Express.js — Revision Guide](Expressjs-Revision-Guide.pdf)
- [MongoDB — Revision Guide](MongoDB-Revision-Guide.pdf)
- [MongoDB Aggregation — Interview Revision Guide](MongoDB_Aggregation_Interview_Revision_Guide_COMPLETE.pdf)
- [NestJS — Overview Guide](NestJS-Overview-Guide.pdf)
- [Node.js Internals — Revision Guide](Nodejs-Internals-Revision-Guide.pdf)
- [Java 8 Stream API — Interview Cheat Sheet](Java_8_Stream_API_Interview_Cheat_Sheet.pdf)
- [Java 8 Stream API — Interview Cheat Sheet (Final)](Java_8_Stream_API_Interview_Cheat_Sheet_Final.pdf)

## Interview Questions ![Interview Questions](https://img.shields.io/badge/-Practice-orange?style=for-the-badge)

- [EPAM — Round 2 Questions](interview-questions/epam-round2-questions.pdf)
- [WeKan — Round Debrief and Fix Pack](interview-questions/WeKan-Round-Debrief-and-Fix-Pack.pdf)

## Design Patterns ![Design Patterns](https://img.shields.io/badge/-Fundamentals-2E8B57?style=for-the-badge)

- [Strategy](src/strategypattern/StrategyPatternDemo.java) — swap payment method at runtime
- [Factory](src/factorypattern/FactoryPatternDemo.java) — create shapes without `new` everywhere
- [Singleton](src/singletonpattern/SingletonPatternDemo.java) — one shared DB connection
- [Proxy](src/proxypattern/ProxyPatternDemo.java) — how Spring's `@Transactional` works
- [Observer](src/observerpattern/ObserverPatternDemo.java) — YouTube channel notifying subscribers

## Low-Level Design ![Low-Level Design](https://img.shields.io/badge/-System%20Design-0A7CFF?style=for-the-badge)

### ![Parking Lot](https://img.shields.io/badge/Parking%20Lot-0A7CFF)
- [Single Level](src/parkinglot/singlelevel/ParkingLot.java)
- [Multi Level](src/parkinglot/multilevel/MultiLevelParkingLot.java)
- [Dynamic Pricing — strategies](src/parkinglot/pricing/DynamicPricingDemo.java)
- [Dynamic Pricing — full lot](src/parkinglot/pricing/DynamicPricingParkingLot.java)

### ![Elevator System](https://img.shields.io/badge/Elevator%20System-8A2BE2)
- [Full implementation](src/elevator/ElevatorController.java) — SCAN algorithm + hall call dispatch
- [Blueprint](src/elevator/blueprint/ElevatorController.java)

### ![BookMyShow](https://img.shields.io/badge/BookMyShow-FF6F00)
- [Full implementation](src/bookmyshow/BookingSystem.java)
- [Blueprint](src/bookmyshow/blueprint/BookingSystem.java)

### ![Splitwise](https://img.shields.io/badge/Splitwise-2E8B57)
- [Full implementation](src/splitwise/SplitwiseService.java) — split strategies, balance ledger, debt simplification
- [Blueprint](src/splitwise/blueprint/SplitwiseService.java)

### ![Snake and Ladder](https://img.shields.io/badge/Snake%20and%20Ladder-DC143C)
- [Full implementation](src/snakeandladder/SnakeAndLadderGame.java)
- [Blueprint](src/snakeandladder/blueprint/SnakeAndLadderGame.java)

### ![Rate Limiter](https://img.shields.io/badge/Rate%20Limiter-008080)
- [Full implementation](src/ratelimiter/RateLimiter.java) — Factory + Strategy pattern, Token Bucket & Sliding Window Log
- [Blueprint](src/ratelimiter/blueprint/RateLimiter.java)

## Node.js API Design ![Node.js API Design](https://img.shields.io/badge/-Backend-339933?style=for-the-badge)

### ![PAN Card KYC Service](https://img.shields.io/badge/PAN%20Card%20KYC%20Service-339933)
- [Full implementation](nodejs-api-design/pan-card-kyc-service/) — Express API with JWT auth middleware: upload a PAN card image, check verification status, and add/update/delete reviewer messages
- [API design README](nodejs-api-design/pan-card-kyc-service/README.md)

## Practice Problems ![Practice Problems](https://img.shields.io/badge/-Problems-DC143C?style=for-the-badge)

- [TwoSum](src/TwoSum.java)
- [LongestUniqueSubString](src/LongestUniqueSubString.java)
- [LongestSubstringWithKUniques](src/LongestSubstringWithKUniques.java)
- [LongestWordsUppercased](src/LongestWordsUppercased.java)
- [MergeTwoSortedLinkedList](src/MergeTwoSortedLinkedList.java)
- [FrequencyMapFilteredStream](src/FrequencyMapFilteredStream.java)
- [RemoveStringFromListUsingStream](src/RemoveStringFromListUsingStream.java)
- [productofallotherelements](src/productofallotherelements.java)
- [Employee](src/Employee.java)
