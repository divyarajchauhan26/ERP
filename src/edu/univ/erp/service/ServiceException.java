package edu.univ.erp.service;

// Make this a public class so all services can use it
public class ServiceException extends Exception {
    public ServiceException(String message) {
        super(message);
    }
}