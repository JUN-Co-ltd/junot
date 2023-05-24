package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * JUNoT利用時間Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvailableTimeModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String availableTimes;

    private String message;
}
