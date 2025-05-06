package com.yourorg.finance.model;

import java.time.LocalDateTime;

public class Reminder {
    public enum Interval { NONE, HOURLY, DAILY, MONTHLY, YEARLY }

    private int id;
    private final int userId;
    private String message;
    private LocalDateTime triggerAt;
    private boolean recurring;
    private Interval interval;
    private long repeatIntervalMs;

    public Reminder(int id,
                    int userId,
                    String message,
                    LocalDateTime triggerAt,
                    boolean recurring,
                    Interval interval,
                    long repeatIntervalMs)
    {
        this.id               = id;
        this.userId           = userId;
        this.message          = message;
        this.triggerAt        = triggerAt;
        this.recurring        = recurring;
        this.interval         = interval;
        this.repeatIntervalMs = repeatIntervalMs;
    }

    // getters & setters

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public int getUserId()                { return userId; }

    public String getMessage()            { return message; }
    public void setMessage(String m)      { this.message = m; }

    public LocalDateTime getTriggerAt()   { return triggerAt; }
    public void setTriggerAt(LocalDateTime t) { this.triggerAt = t; }

    public boolean isRecurring()          { return recurring; }
    public void setRecurring(boolean r)   { this.recurring = r; }

    public Interval getInterval()         { return interval; }
    public void setInterval(Interval i)   { this.interval = i; }

    public long getRepeatIntervalMs()         { return repeatIntervalMs; }
    public void setRepeatIntervalMs(long ms)  { this.repeatIntervalMs = ms; }
}
