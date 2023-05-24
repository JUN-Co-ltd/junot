package jp.co.jun.edi.component.model;

import lombok.Data;

/**
 * 取引先のキーのModel.
 */
@Data
public class MaintSireKeyModel {

    /** 仕入先コード. */
    private final String sireCode;

    /** 工場コード. */
    private final String kojCode;

    /**
     * @param sireCode 仕入先コード
     * @param kojCode 工場コード
     */
    public MaintSireKeyModel(final String sireCode, final String kojCode) {
        this.sireCode = sireCode;
        this.kojCode = kojCode;
    }
}
