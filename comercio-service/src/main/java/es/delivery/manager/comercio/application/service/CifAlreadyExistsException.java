package es.delivery.manager.comercio.application.service;

public class CifAlreadyExistsException extends RuntimeException {
    public CifAlreadyExistsException(String cif) {
        super("Ya existe un comercio con CIF " + cif);
    }
}
