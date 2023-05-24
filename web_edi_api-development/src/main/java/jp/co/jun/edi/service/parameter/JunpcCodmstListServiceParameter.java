package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.model.JunpcCodmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import jp.co.jun.edi.type.MCodmstTblIdType;
import lombok.Builder;
import lombok.Getter;

/**
 * 発注生産システムのコードマスタの検索パラメーター.
 */
@Getter
public class JunpcCodmstListServiceParameter extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    private final MCodmstTblIdType tblId;

    /** 検索条件. */
    private final JunpcCodmstSearchConditionModel searchCondition;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param tblId {@link MCodmstTblIdType} instance
     * @param searchCondition {@link JunpcCodmstSearchConditionModel} instance
     */
    @Builder
    public JunpcCodmstListServiceParameter(final CustomLoginUser loginUser, final MCodmstTblIdType tblId,
            final JunpcCodmstSearchConditionModel searchCondition) {
        super(loginUser);
        this.tblId = tblId;
        this.searchCondition = searchCondition;
    }
}
