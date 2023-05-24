package jp.co.jun.edi.entity.extended;

import java.util.Map;

import jp.co.jun.edi.entity.GenericEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張画面構成情報のEntity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedMScreenStructureEntity extends GenericEntity {
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
