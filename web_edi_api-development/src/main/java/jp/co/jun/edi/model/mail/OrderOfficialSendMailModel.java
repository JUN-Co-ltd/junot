package jp.co.jun.edi.model.mail;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 発注書PDF[夜間]のメール送信先用データModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderOfficialSendMailModel  implements Serializable {
    private static final long serialVersionUID = 1L;

    /** タイトル.接頭語. */
    private String subjectPrefix;

    /** 署名. */
    private String signature;
}

