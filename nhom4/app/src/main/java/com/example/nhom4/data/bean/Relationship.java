package com.example.nhom4.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.List;

public class Relationship {
    private String relationshipId;
    private List<String> members;
    @ServerTimestamp
    private Timestamp createdAt;

    public Relationship() {}

    public Relationship(List<String> members) {
        this.members = members;
    }

    public String getRelationshipId() { return relationshipId; }
    public List<String> getMembers() { return members; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setRelationshipId(String relationshipId) { this.relationshipId = relationshipId; }
    public void setMembers(List<String> members) { this.members = members; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
