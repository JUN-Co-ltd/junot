package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 画面構成Model.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ScreenSettingStructureModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** キー. */
    private String key;
    /** 名称. */
    private String name;
    /** 画面構成種別. */
    private String type;
    /** 画面構成定義. */
    private Map<String, String> validators;
}
