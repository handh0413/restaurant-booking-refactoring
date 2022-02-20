package com.cleancode.restaurant;

public class TestableSmsSender extends SmsSender {
    private boolean sendMethodIsCalled;

    @Override
    public void send(Schedule schedule) {
        System.out.println("테스터블 SMS 클래스 send 메서드 실행");
        sendMethodIsCalled = true;
    }

    public boolean isSendMethodIsCalled() {
        return sendMethodIsCalled;
    }
}
