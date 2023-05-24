package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.model.maint.code.MaintCodeSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import jp.co.jun.edi.type.MCodmstTblIdType;
import lombok.Builder;
import lombok.Getter;

/**
 * メンテナンスコードの検索パラメーター.
 */
@Getter
public class MaintCodeSearchServiceParameter extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    private final MCodmstTblIdType tblId;

    /** 検索条件. */
    private final MaintCodeSearchConditionModel searchCondition;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param tblId {@link MCodmstTblIdType} instance
     * @param searchCondition {@link MaintCodeSearchConditionModel} instance
     */
    @Builder
    public MaintCodeSearchServiceParameter(final CustomLoginUser loginUser, final MCodmstTblIdType tblId,
            final MaintCodeSearchConditionModel searchCondition) {
        super(loginUser);
        this.tblId = tblId;
        this.searchCondition = searchCondition;
    }
}
