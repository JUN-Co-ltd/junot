package jp.co.jun.edi.model.mail;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品依頼承認時のメール送信先用データModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryOfficialSendMailModel  implements Serializable {
    private static final long serialVersionUID = 1L;

    /** タイトル.接頭語. */
    private String subjectPrefix;

    /** 署名. */
    private String signature;
}

