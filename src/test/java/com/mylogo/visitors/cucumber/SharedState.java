package com.mylogo.visitors.cucumber;

import org.springframework.stereotype.Component;

@Component
public class SharedState {
    private Long visitorId;

    public Long getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(Long visitorId) {
        this.visitorId = visitorId;
    }
}
