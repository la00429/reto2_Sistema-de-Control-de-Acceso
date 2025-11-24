package com.accesscontrol.login.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Modelo Login según especificación:
 * - id: SERIAL (Autoincrement) - PRIMARY KEY, Not Null
 * - userID: BIGINTEGER - Not Null
 * - password: VARCHAR(20) - Not Null
 */
@Document(collection = "Login")
public class Login {
    @Id
    private String id;
    
    private Long userID;  // BIGINTEGER según especificación
    
    private String password;  // VARCHAR(20) según especificación

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}





