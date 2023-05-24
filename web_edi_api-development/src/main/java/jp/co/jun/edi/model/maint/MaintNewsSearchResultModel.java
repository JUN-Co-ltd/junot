package jp.co.jun.edi.model.maint;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * マスタメンテナンス用のお知らせ検索結果のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintNewsSearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** タイトル. */
    private String title;

    /** 公開開始日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date openStartAt;

    /** 公開終了日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date openEndAt;

    /** 新着表示終了日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date newDisplayEndAt;
}
