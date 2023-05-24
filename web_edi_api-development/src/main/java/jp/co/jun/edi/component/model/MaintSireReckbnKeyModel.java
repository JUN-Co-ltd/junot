package jp.co.jun.edi.component.model;

import lombok.Data;

/**
 * 取引先のキーのModel(削除用).
 */
@Data
public class MaintSireReckbnKeyModel {

    /** 仕入先コード. */
    private final String sireCode;

    /** 工場コード. */
    private final String kojCode;

    /** 区分. */
    private final String reckbn;

    /**
     * @param sireCode 仕入先コード
     * @param kojCode 工場コード
     * @param reckbn 区分
     */
    public MaintSireReckbnKeyModel(final String sireCode, final String kojCode, final String reckbn) {
        this.sireCode = sireCode;
        this.kojCode = kojCode;
        this.reckbn = reckbn;
    }
}
