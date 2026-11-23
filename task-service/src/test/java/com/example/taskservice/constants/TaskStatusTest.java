package com.example.taskservice.constants;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Timur Khabibullin
 */
class TaskStatusTest {

    @CsvSource({"NEW,IN_PROGRESS", "IN_PROGRESS,DONE"})
    @ParameterizedTest
    void shouldAllowForwardTransition(TaskStatus from, TaskStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @CsvSource({"NEW,NEW", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    @ParameterizedTest
    void shouldRejectOtherTransitions(TaskStatus from, TaskStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }
}
