package com.inventory.entity;

public enum PartType {
    ENGINE("엔진"),
    TRANSMISSION("변속기"),
    BRAKE("브레이크"),
    SUSPENSION("서스펜션"),
    ELECTRICAL("전기"),
    BODY("차체"),
    INTERIOR("내장"),
    EXTERIOR("외장"),
    FILTER("필터"),
    LUBRICANT("윤활유"),
    TIRE("타이어"),
    WHEEL("휠"),
    EXHAUST("배기"),
    COOLING("냉각"),
    FUEL("연료"),
    IGNITION("점화"),
    CHARGING("충전"),
    LIGHTING("조명"),
    SENSOR("센서"),
    ACTUATOR("액추에이터"),
    OTHER("기타");

    private final String description;

    PartType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}