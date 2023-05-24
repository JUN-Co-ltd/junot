package jp.co.jun.edi.service.parameter;

import jp.co.jun.edi.model.maint.MaintSizeSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericServiceParameter;
import lombok.Builder;
import lombok.Getter;
//PRD_0137 #10669 add start

/**
 * サイズマスタの検索パラメーター.
 */
@Getter
public class MaintSizeSearchServiceParameter extends GenericServiceParameter {
    private static final long serialVersionUID = 1L;

    private final String hscd;

    /** 検索条件. */
    private final MaintSizeSearchConditionModel searchCondition;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param hscd {@link String} instance
     * @param searchCondition {@link MaintSizeSearchConditionModel} instance
     */
    @Builder
    public MaintSizeSearchServiceParameter(final CustomLoginUser loginUser, final String hscd,
            final MaintSizeSearchConditionModel searchCondition) {
        super(loginUser);
        this.hscd = hscd;
        this.searchCondition = searchCondition;
    }
}
//PRD_0137 #10669 add end