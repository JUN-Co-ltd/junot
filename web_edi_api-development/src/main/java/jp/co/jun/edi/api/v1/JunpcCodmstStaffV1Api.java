package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.JunpcCodmstModel;
import jp.co.jun.edi.model.JunpcCodmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.JunpcCodmstStaffGetService;
import jp.co.jun.edi.service.JunpcCodmstStaffListService;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注生産システムのコードマスタから社員情報を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/codmst/staffs")
public class JunpcCodmstStaffV1Api extends GenericJunpcCodmstV1Api {
    @Autowired
    private JunpcCodmstStaffGetService getService;

    @Autowired
    private JunpcCodmstStaffListService listService;

    /**
     * 発注生産システムのコードマスタから社員情報をリストで取得する.
     *
     * @param loginUser 認証情報
     * @param model モデル
     * @return 社員情報のリスト
     */
    @GetMapping
    public GenericListMobel<JunpcCodmstModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated final JunpcCodmstSearchConditionModel model) {
        return search(loginUser, model);
    }

    /**
     * 発注生産システムのコードマスタから社員情報を取得する.
     *
     * @param loginUser 認証情報
     * @param accountName アカウント名
     * @return 社員情報のリスト
     */
    @GetMapping("/{accountName}")
    public JunpcCodmstModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("accountName") final String accountName) {
        return getService.call(GetServiceParameter.<String>builder().loginUser(loginUser).id(accountName).build()).getItem();
    }

    @Override
    protected MCodmstTblIdType getTblId() {
        return MCodmstTblIdType.STAFF;
    }

    @Override
    protected JunpcCodmstStaffListService getListService() {
        return listService;
    }
}
