/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BatchScheduleTest {

  private static final Instant SENT = Instant.parse("2026-11-26T13:00:00Z");

  @Test
  void recommendedFiguresOfTheGuide() {
    BatchSchedule schedule = BatchSchedule.recommended();

    assertThat(schedule.firstQueryDelay()).isEqualTo(Duration.ofMinutes(10));
    assertThat(schedule.minimumInterval()).isEqualTo(Duration.ofMinutes(10));
    assertThat(schedule.queryWindow()).isEqualTo(Duration.ofHours(48));
  }

  @Test
  void firstAndNextQueries() {
    BatchSchedule schedule = BatchSchedule.recommended();

    assertThat(schedule.firstQueryAt(SENT)).isEqualTo(Instant.parse("2026-11-26T13:10:00Z"));
    assertThat(schedule.nextQueryAt(Instant.parse("2026-11-26T13:12:30Z")))
        .isEqualTo(Instant.parse("2026-11-26T13:22:30Z"));
  }

  @Test
  void windowOfFortyEightHours() {
    BatchSchedule schedule = BatchSchedule.recommended();
    Instant end = Instant.parse("2026-11-28T13:00:00Z");

    assertThat(schedule.windowEndsAt(SENT)).isEqualTo(end);
    assertThat(schedule.windowExpired(SENT, SENT)).isFalse();
    assertThat(schedule.windowExpired(SENT, end)).as("up to 48 hours inclusive").isFalse();
    assertThat(schedule.windowExpired(SENT, end.plusSeconds(1))).isTrue();
  }

  @Test
  void customSchedule() {
    BatchSchedule schedule =
        BatchSchedule.of(Duration.ofMinutes(15), Duration.ofMinutes(30), Duration.ofHours(24));

    assertThat(schedule.firstQueryAt(SENT)).isEqualTo(SENT.plus(Duration.ofMinutes(15)));
    assertThat(schedule.nextQueryAt(SENT)).isEqualTo(SENT.plus(Duration.ofMinutes(30)));
    assertThat(schedule.windowExpired(SENT, SENT.plus(Duration.ofHours(25)))).isTrue();
    assertThat(schedule)
        .isEqualTo(
            BatchSchedule.of(Duration.ofMinutes(15), Duration.ofMinutes(30), Duration.ofHours(24)))
        .hasSameHashCodeAs(
            BatchSchedule.of(Duration.ofMinutes(15), Duration.ofMinutes(30), Duration.ofHours(24)))
        .isNotEqualTo(BatchSchedule.recommended());
    assertThat(schedule.toString()).contains("PT15M", "PT30M", "PT24H");
  }

  @Test
  void rejectsDurationsOutOfRange() {
    Duration ten = Duration.ofMinutes(10);
    assertThat(BatchSchedule.of(Duration.ZERO, ten, ten).firstQueryAt(SENT)).isEqualTo(SENT);
    assertThatThrownBy(() -> BatchSchedule.of(Duration.ofMinutes(-1), ten, ten))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> BatchSchedule.of(ten, Duration.ZERO, ten))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> BatchSchedule.of(ten, ten, Duration.ZERO))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
