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
import jp.co.jun.edi.service.JunpcCodmstColorListService;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注生産システムのコードマスタから事業部情報を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/codmst/divisions")
public class JunpcCodmstDivisionV1Api extends GenericJunpcCodmstV1Api {
    @Autowired
    private JunpcCodmstColorListService listService;

    /**
     * 発注生産システムのコードマスタから事業部情報を一覧で取得する.
     *
     * @param loginUser 認証情報
     * @param model モデル
     * @return カラー情報一覧
     */
    @GetMapping
    public GenericListMobel<JunpcCodmstModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated final JunpcCodmstSearchConditionModel model) {
        return search(loginUser, model);
    }

    @Override
    protected MCodmstTblIdType getTblId() {
        return MCodmstTblIdType.DIVISION;
    }

    @Override
    protected JunpcCodmstColorListService getListService() {
        return listService;
    }
}
