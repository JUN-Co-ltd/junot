package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.model.maint.code.MaintCodeBulkUpdateModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import jp.co.jun.edi.type.MCodmstTblIdType;
import lombok.Builder;
import lombok.Getter;

/**
 * メンテナンスコードの検索パラメーター.
 */
@Getter
public class MaintCodeBulkUpdateServiceParameter extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    private final MCodmstTblIdType tblId;

    /** 検索条件. */
    private final MaintCodeBulkUpdateModel bulkUpdateModel;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param tblId {@link MCodmstTblIdType} instance
     * @param bulkUpdateModel {@link MaintCodeBulkUpdateModel} instance
     */
    @Builder
    public MaintCodeBulkUpdateServiceParameter(final CustomLoginUser loginUser, final MCodmstTblIdType tblId,
            final MaintCodeBulkUpdateModel bulkUpdateModel) {
        super(loginUser);
        this.tblId = tblId;
        this.bulkUpdateModel = bulkUpdateModel;
    }
}
