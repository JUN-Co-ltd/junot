package jp.co.jun.edi.model.maint;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * マスタメンテナンス用のお知らせ情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintNewsModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int TITLE_SIZE_MAX = 200;
    private static final int CONTENT_SIZE_MAX = 4000;

    /** ID. */
    private BigInteger id;

    /** タイトル. */
    @NotNull(groups = Default.class)
    @Size(max = TITLE_SIZE_MAX, groups = Default.class)
    private String title;

    /** 本文. */
    @NotNull(groups = Default.class)
    @Size(max = CONTENT_SIZE_MAX, groups = Default.class)
    private String content;

    /** 公開開始日時. */
    @NotNull(groups = Default.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date openStartAt;

    /** 公開終了日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date openEndAt;

    /** 新着表示終了日時. */
    @NotNull(groups = Default.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date newDisplayEndAt;
}
