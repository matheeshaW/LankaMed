package com.lankamed.health.backend.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class WaitlistEntryModelTest {

    @Test
    void prePersist_setsCreatedAt() {
        WaitlistEntry entry = new WaitlistEntry();
        assertNull(entry.getCreatedAt());
        entry.prePersist();
        assertNotNull(entry.getCreatedAt());
        assertTrue(entry.getCreatedAt().isBefore(Instant.now()) || entry.getCreatedAt().equals(Instant.now()));
    }

    @Test
    void builder_defaults() {
        WaitlistEntry entry = WaitlistEntry.builder().build();
        assertEquals(WaitlistEntry.Status.QUEUED, entry.getStatus());
        assertFalse(entry.isPriority());
    }
}
