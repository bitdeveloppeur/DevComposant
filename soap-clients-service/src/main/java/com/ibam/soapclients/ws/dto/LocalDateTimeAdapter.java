package com.ibam.soapclients.ws.dto;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Conversion {@link LocalDateTime} &harr; {@code xs:dateTime} au format ISO-8601
 * ( {@code yyyy-MM-dd'T'HH:mm:ss} ), lisible par tous les clients SOAP.
 */
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime unmarshal(String valeur) {
        return (valeur == null || valeur.isBlank()) ? null : LocalDateTime.parse(valeur, FORMAT);
    }

    @Override
    public String marshal(LocalDateTime valeur) {
        return valeur == null ? null : valeur.format(FORMAT);
    }
}
