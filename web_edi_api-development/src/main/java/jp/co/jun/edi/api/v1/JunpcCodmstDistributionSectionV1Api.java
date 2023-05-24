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
import jp.co.jun.edi.service.JunpcCodmstDistributionSectionService;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注生産システムのコードマスタから事業部情報を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/codmst/distributionSections")
public class JunpcCodmstDistributionSectionV1Api extends GenericJunpcCodmstV1Api {
    @Autowired
    private JunpcCodmstDistributionSectionService listService;

    /**
     * コードマスタから課情報を一覧で取得する.
     *
     * @param loginUser 認証情報
     * @param model モデル
     * @return 課名情報一覧
     */
    @GetMapping
    public GenericListMobel<JunpcCodmstModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated final JunpcCodmstSearchConditionModel model) {
        GenericListMobel<JunpcCodmstModel> list = search(loginUser, model);
        return list;
    }

    @Override
    protected MCodmstTblIdType getTblId() {
        return MCodmstTblIdType.DISTRIBUTION_SECTION;

    }

    @Override
    protected JunpcCodmstDistributionSectionService getListService() {

        return listService;
    }
}
