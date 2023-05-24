package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.JunpcCodmstModel;
import jp.co.jun.edi.model.JunpcCodmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.JunpcCodmstSearchByTblIdAndCode1Service;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注生産システムのコードマスタから配分課を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/codmst/expenseItems")
public class JunpcCodmstExpenseItemV1Api extends GenericJunpcCodmstV1Api {

    @Autowired
    private JunpcCodmstSearchByTblIdAndCode1Service searchService;

    /**
     * 発注生産システムのコードマスタから費目をリストで取得する.
     *
     * @param loginUser 認証情報
     * @param model モデル
     * @return 費目情報のリスト
     */
    @PostMapping("/search")
    public GenericListMobel<JunpcCodmstModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({ JunpcCodmstSearchConditionModel.Code1.class }) final JunpcCodmstSearchConditionModel model) {
        return search(loginUser, model);
    }

    @Override
    protected MCodmstTblIdType getTblId() {
        return MCodmstTblIdType.EXPENSE_ITEM;
    }

    @Override
    protected JunpcCodmstSearchByTblIdAndCode1Service getListService() {
        return searchService;
    }
}
