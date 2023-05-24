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
import jp.co.jun.edi.service.JunpcCodmstSearchByBrandListService;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注生産システムのコードマスタからテイスト情報を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/codmst/tastes")
public class JunpcCodmstTasteV1Api extends GenericJunpcCodmstV1Api {
    @Autowired
    private JunpcCodmstSearchByBrandListService listService;

    /**
     * 発注生産システムのコードマスタからテイスト情報をリストで取得する.
     *
     * @param loginUser 認証情報
     * @param model モデル
     * @return テイスト情報のリスト
     */
    @GetMapping
    public GenericListMobel<JunpcCodmstModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated({ JunpcCodmstSearchConditionModel.Brand.class }) final JunpcCodmstSearchConditionModel model) {
        return search(loginUser, model);
    }

    @Override
    protected MCodmstTblIdType getTblId() {
        return MCodmstTblIdType.TASTE;
    }

    @Override
    protected JunpcCodmstSearchByBrandListService getListService() {
        return listService;
    }
}
