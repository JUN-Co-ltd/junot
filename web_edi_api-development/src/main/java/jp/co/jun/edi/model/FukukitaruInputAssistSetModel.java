package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 入力補助セットのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruInputAssistSetModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** セット名. */
    @Column(name = "set_name")
    private String setName;

    /** 入力補助セット詳細. */
    private List<FukukitaruInputAssistSetDetailsModel> listInputAssistSetDetails;
}
