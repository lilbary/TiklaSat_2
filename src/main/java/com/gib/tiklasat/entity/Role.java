package com.gib.tiklasat.entity;

/**
 * BR-U-002 — Sistemdeki roller.
 *
 * "Ziyaretçi" bilerek burada YOK: o bir rol değil, kimlik doğrulamasının
 * yokluğu. Giriş yapmamış birinin veritabanında karşılığı olmaz.
 *
 * Bir kullanıcı aynı anda birden fazla role sahip olabilir (BR-U-003),
 * bu yüzden User entity'sinde tek bir alan değil bir küme tutuluyor.
 */
public enum Role {
    BUYER,      // kayıt olan herkes otomatik alır
    SELLER,     // ilk ilan yayınlandığında eklenir
    ADMIN,
    MODERATOR
}
