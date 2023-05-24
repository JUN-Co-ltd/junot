package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.JunpcCodmstModel;
import jp.co.jun.edi.model.JunpcCodmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.JunpcCodmstAllocationListService;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注生産システムのコードマスタから配分課を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/codmst/allocations")
public class JunpcCodmstAllocationV1Api extends GenericJunpcCodmstV1Api {
    @Autowired
    private JunpcCodmstAllocationListService listService;

    /**
     * 発注生産システムのコードマスタから配分課をリストで取得する.
     *
     * @param loginUser 認証情報
     * @param model モデル
     * @return 配分課情報のリスト
     */
    @GetMapping
    public GenericListMobel<JunpcCodmstModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated final JunpcCodmstSearchConditionModel model) {
        return search(loginUser, model);
    }

    @Override
    protected MCodmstTblIdType getTblId() {
        return MCodmstTblIdType.ALLOCATION;
    }

    @Override
    protected JunpcCodmstAllocationListService getListService() {
        return listService;
    }
}
