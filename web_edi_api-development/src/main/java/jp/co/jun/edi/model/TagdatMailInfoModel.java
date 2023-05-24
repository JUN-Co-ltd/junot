package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * TAGDATメール情報用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagdatMailInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** タイトル.接頭語. */
    private String subjectPrefix;

    /** 署名. */
    private String signature;

    /** 本文：メッセージ. */
    private List<TagdatMailInfoBrandCountModel> messages;
}
