package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.SpecialtyQubeCancelStatusType;
import lombok.Data;

/**
 * 店別配分キャンセルAPIレスポンス情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpecialtyQubeCancelResponseModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 処理時間. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd hh:mm:ss.SSSSSS")
    private LocalDateTime processtime;

    /** ステータス. */
    private SpecialtyQubeCancelStatusType status;

    /** SQロックユーザーID. */
    private BigInteger sqLockUserId;

    /** エラーリスト. */
    private List<String> errorList;
}
