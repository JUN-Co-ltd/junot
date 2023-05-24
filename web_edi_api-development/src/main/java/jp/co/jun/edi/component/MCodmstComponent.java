package jp.co.jun.edi.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.OccupationType;

/**
 * コードマスタ関連のコンポーネント.
 */
@Component
public class MCodmstComponent extends GenericComponent {

    @Autowired
    private MCodmstRepository mCodmstRepository;

    // QAセンターのメニュー
    private static final String QA_MENU = "100";
    // 優良誤認検査承認
    private static final String QA_APPROVAL = "01";

    /**
     * ログインユーザの発注承認可ブランドリストを取得する.
     *
     * @param accountName ログインユーザのアカウント名
     * @return 発注承認可ブランドリスト
     */
    public List<String> getOrderApprovalAuthorityBlands(final String accountName) {
        List<String> orderApprovalAuthorityBlands = new ArrayList<>();

        List<MCodmstEntity> mCodmstEntityList = mCodmstRepository.findByTblidAndCode1OrderById(
                MCodmstTblIdType.STAFF.getValue(), accountName, PageRequest.of(0, 1)).getContent();

        if (!mCodmstEntityList.isEmpty()) {
            MCodmstEntity userEntity = mCodmstEntityList.get(0);
            if (!StringUtils.isEmpty(userEntity.getItem4())) {
                final String trimToItem4 = StringUtils.trimToEmpty(userEntity.getItem4());
                final String brands = StringUtils.right(trimToItem4, StringUtils.length(trimToItem4) - 1);
                // ブランドを2桁ずつ分割
                orderApprovalAuthorityBlands = jp.co.jun.edi.util.StringUtils.splitByLength(brands, 2);
            }
        }

        return orderApprovalAuthorityBlands;
    }

    /**
     * QA権限か判定する.
     * @param accountName アカウント名
     * @return true:QA権限
     */
    public boolean isQaAuth(final String accountName) {
        // QA情報取得
        final List<MCodmstEntity> qaEntityList = mCodmstRepository.findByTblidAndCode1AndCode2OrderById(
                MCodmstTblIdType.MENU.getValue(), QA_MENU, QA_APPROVAL, PageRequest.of(0, 1)).getContent();
        if (qaEntityList.isEmpty()) {
            return false;
        }

        // ログインユーザー情報取得
        final MCodmstEntity staffEntity = mCodmstRepository.findByTblidAndCode1(
                MCodmstTblIdType.STAFF.getValue(), accountName).orElse(null);

        if (ObjectUtils.isEmpty(staffEntity)) {
            return false;
        }

        // QA権限キー
        final String qaAuthKey = qaEntityList.get(0).getItem2();

        if (StringUtils.isEmpty(qaAuthKey) || qaAuthKey.length() < 2) {
            return false;
        }

        // 2文字ずつ分割
        final List<String> qaAuthKeys = jp.co.jun.edi.util.StringUtils.splitByLength(qaAuthKey.trim(), 2);

        // 社員権限キー
        final String staffAuth = staffEntity.getItem3();

        // 社員権限キーの2文字目
        final char staffAuthKeySecondChar = staffAuth.charAt(1);

        if (Character.isDigit(staffAuthKeySecondChar)) {
            // 社員権限キーの2文字目が数字の場合の合致判定
            return qaAuthKeys.stream().anyMatch(qaKey -> isQaAuthAtSecondCharDigit(qaKey, staffAuth.charAt(0), staffAuthKeySecondChar));
        }

        // 社員権限キーの2文字目が数字でなければそのまま比較
        return qaAuthKeys.contains(staffAuth.substring(0, 2));
    }

    /**
     * 社員権限キーの2文字目が数字の場合のQA権限判定.
     * 社員権限キーとQA権限キーの1文字目合致
     *  かつ QA権限キーの2文字目数字
     *  かつ 社員権限キーの数字がQA権限キーの数字以下
     * であればQA権限である.
     * @param qaAuthKey QA権限キー
     * @param staffAuthKeySecondChar 社員権限キーの1文字目
     * @param staffAuthKeyFirstChar 社員権限キーの2文字目
     * @return true:QA権限
     */
    private boolean isQaAuthAtSecondCharDigit(final String qaAuthKey, final char staffAuthKeyFirstChar, final char staffAuthKeySecondChar) {
        final char qaSecondChar = qaAuthKey.charAt(1);
        return staffAuthKeyFirstChar == qaAuthKey.charAt(0)
                && Character.isDigit(qaSecondChar)
                && Character.getNumericValue(staffAuthKeySecondChar) <= Character.getNumericValue(qaSecondChar);
    }

    /**
     * ログインユーザの職種を取得する.
     *
     * @param accountName ログインユーザのアカウント名
     * @return ログインユーザの職種
     */
    public OccupationType getOccupationType(final String accountName) {
        return OccupationType.convertToType(mCodmstRepository.findItem7ByTblidAndCode1(MCodmstTblIdType.STAFF.getValue(), accountName));
    }

    // PRD_0131 #10039 add JFE start
    /**
     * 課コード、ブランドを基に物流コードを取得する.
     *
     * @param divisionCode 課コード(code1の下２桁）
     * @param brandCode ブランドコード(code1の上２桁)
     * @return 物流コード（コードマスタtblid:25のitem3の先頭2文字）
     */
    public String getLogisticsCode(final String divisionCode,final String brandCode) {
        return mCodmstRepository.findItem3ByTblidAndCode1(MCodmstTblIdType.ALLOCATION.getValue(), divisionCode,brandCode);
    }
    // PRD_0131 #10039 add JFE end
}
