package com.example.taskservice.constants;

/**
 * Статус задачи
 *
 * @author Timur Khabibullin
 */
public enum TaskStatus {

    /**
     * Новая
     */
    NEW,

    /**
     * В работе
     */
    IN_PROGRESS,

    /**
     * Выполнена
     */
    DONE;

    /**
     * Допустимые переходы: NEW -> IN_PROGRESS -> DONE
     *
     * @param target целевой статус
     * @return true, если переход из текущего статуса в целевой разрешён
     */
    public boolean canTransitionTo(TaskStatus target) {
        return switch (this) {
            case NEW -> target == IN_PROGRESS;
            case IN_PROGRESS -> target == DONE;
            case DONE -> false;
        };
    }
}
