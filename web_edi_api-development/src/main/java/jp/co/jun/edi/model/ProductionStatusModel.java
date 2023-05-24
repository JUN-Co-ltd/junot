package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.ProductionStatusType;
import lombok.Data;

/**
 * 生産ステータスのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductionStatusModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 発注ID. */
    private BigInteger orderId;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 生産ステータス. */
    private ProductionStatusType productionStatusType;

    /** サンプル上がり予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date sampleCompletionAt;

    /** サンプル上がり確定予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date sampleCompletionFixAt;

    /** 仕様確定予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date specificationAt;

    /** 仕様確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date specificationFixAt;

    /** 生地入荷予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date textureArrivalAt;

    /** 生地入荷確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date textureArrivalFixAt;

    /** 付属入荷予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date attachmentArrivalAt;

    /** 付属入荷確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date attachmentArrivalFixAt;

    /** 上がり予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date completionAt;

    /** 上がり確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date completionFixAt;

    /** 上がり総数. */
    private Integer completionCount;

    /** 縫製検品到着予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date sewInspectionAt;

    /** 縫製検品到着確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date sewInspectionFixAt;

    /** 検品実施予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date inspectionAt;

    /** 検品実施確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date inspectionFixAt;

    /** 出港予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date leavePortAt;

    /** 出港確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date leavePortFixAt;

    /** 入港予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date enterPortAt;

    /** 入港確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date enterPortFixAt;

    /** 通関予定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date customsClearanceAt;

    /** 通関確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date customsClearanceFixAt;

    /** DISTA入荷日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date distaArrivalAt;

    /** DISTA確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date distaArrivalFixAt;

    /** メモ. */
    private String memo;
}
