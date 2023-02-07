package com.otcengineering.white_app.utils;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTimer extends Timer {
    private long m_startTime;
    private TimerTask m_timerTask;

    public long getExecutionTime() {
        return System.currentTimeMillis() - m_startTime;
    }

    public TimerTimer(String name) {
        super(name);
    }

    @Override
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        super.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                m_startTime = System.currentTimeMillis();
                task.run();
            }
        }, delay, period);
    }

    public void setTimerTask(TimerTask timerTask) {
        m_timerTask = timerTask;
    }

    public void schedule() {
        scheduleAtFixedRate(m_timerTask, 0, 1);
    }
}
