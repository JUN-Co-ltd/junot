package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.MisleadingRepresentationType;
import lombok.Data;

/**
 * 優良誤認検査情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MisleadingRepresentationModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 優良誤認検査対象区分. */
    private MisleadingRepresentationType misleadingRepresentationType;

    /** 色コード. */
    private String colorCode;

    /** 原産国コード. */
    private String cooCode;

    /** 生産メーカーコード. */
    private String mdfMakerCode;

    /** 検査承認日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date approvalAt;

    /** 承認者アカウント名. */
    private String approvalUserAccountName;

    /** 承認者アカウント名称. */
    private String approvalUserName;

    /** memo. */
    private String memo;

    /** 更新日時. */
    @NotNull(groups = Default.class)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date updatedAt;
}
