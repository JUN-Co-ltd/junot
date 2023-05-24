package jp.co.jun.edi.entity.schedule;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 指示ファイル作成共通のEntity.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkingCreateCsvFileCommonEntity implements Serializable {
    private static final long serialVersionUID = 1L;

}
