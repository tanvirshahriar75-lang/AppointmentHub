package com.tanvir.appointmenthub.models;

public class Appointment {
    private String id;
    private String requestedToId;
    private String requestedById;
    private String status;
    private String title;

    public Appointment() { }

    public Appointment(String id, String requestedToId, String requestedById, String status, String title) {
        this.id = id;
        this.requestedToId = requestedToId;
        this.requestedById = requestedById;
        this.status = status;
        this.title = title;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRequestedToId() { return requestedToId; }
    public void setRequestedToId(String requestedToId) { this.requestedToId = requestedToId; }

    public String getRequestedById() { return requestedById; }
    public void setRequestedById(String requestedById) { this.requestedById = requestedById; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}

