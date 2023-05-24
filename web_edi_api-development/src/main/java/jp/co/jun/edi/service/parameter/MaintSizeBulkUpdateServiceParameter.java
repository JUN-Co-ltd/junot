//PRD_0137 #10669 add start
package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.model.maint.MaintSizeBulkUpdateModel;
import jp.co.jun.edi.model.maint.code.MaintCodeBulkUpdateModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import jp.co.jun.edi.type.MCodmstTblIdType;
import lombok.Builder;
import lombok.Getter;

/**
 *サイズ情報の検索パラメーター.
 */
@Getter
public class MaintSizeBulkUpdateServiceParameter extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    private final String hscd;

    private final Boolean copyFlg;

    /** 検索条件. */
    private final MaintSizeBulkUpdateModel bulkUpdateModel;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param tblId {@link MCodmstTblIdType} instance
     * @param bulkUpdateModel {@link MaintCodeBulkUpdateModel} instance
     */
    @Builder
    public MaintSizeBulkUpdateServiceParameter(final CustomLoginUser loginUser, final String hscd,
            final MaintSizeBulkUpdateModel bulkUpdateModel,final Boolean copyFlg) {
        super(loginUser);
        this.hscd = hscd;
        this.copyFlg = copyFlg;
        this.bulkUpdateModel = bulkUpdateModel;
    }
}
//PRD_0137 #10669 add end