package jp.co.jun.edi.repository.specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.type.MCodmstSearchType;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.OccupationType;
import jp.co.jun.edi.type.StaffType;

/**
 * コードマスタ　担当検索動的検索条件設定クラス.
 */
@Component
public class JunpcCodmstStaffSpecification {


    @Autowired
    private MCodmstRepository mCodmstRepository;


    /**
     * 削除日時がNULLでない.
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> notDeleteContains() {
        return (root, query, cb) -> {
            return cb.isNull(root.get("deletedAt"));
        };
    }

    /**
     * メンテ区分が1,2,空白のいずれか.
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> mntflgContains() {

        List<String> activeMntflgList = Arrays.asList(new String[]{"1", "2", ""});

        return (root, query, cb) -> {
            return root.get("mntflg").in(activeMntflgList);
        };
    }

    /**
     * テーブルID条件を設定.
     *
     * @param tblid テーブルID
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> tblidContains(final String tblid) {

        return (root, query, cb) -> {
            return cb.equal(root.get("tblid"), tblid);
        };
    }

    /**
     * JUNoT検索対象判定フラグが1.
     *
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> junotSearchTargetContains() {

        return (root, query, cb) -> {
            return cb.equal(root.get("item5"), "1");
        };
    }

    /**
     * 担当者コード/担当者名検索条件設定(OR条件).
     * 検索タイプによって部分一致またはlike検索を行う
     *
     * @param searchType 検索タイプ
     * @param searchText 検索文字列
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> staffCodeOrStaffNameContains(final String searchType, final String searchText) {

        if (StringUtils.isEmpty(searchText)) {
            // 検索文字列が無ければ絞り込み検索は行わない。
            return null;
        }

        switch (MCodmstSearchType.findByValue(searchType).orElse(MCodmstSearchType.LIKE_CODE_OR_NAME)) {
        case LIKE_CODE_OR_NAME:
            // コード/名称 LIKE検索
            return staffCodeLikeContains(searchText).or(staffNameLikeContains(searchText));
        case LIKE_CODE:
            // コード LIKE検索
            return staffCodeLikeContains(searchText);
        case LIKE_NAME:
            // 名称 LIKE検索
            return staffNameLikeContains(searchText);
        case CODE:
            // コード 完全一致検索
            return staffCodeMatchContains(searchText);

        default:
            // コード/名称 LIKE検索
            return staffCodeLikeContains(searchText).or(staffNameLikeContains(searchText));
        }
    }

    /**
     * 担当者コードの絞り込み検索条件(like検索).
     *
     * @param staffCode 担当者コード
     * @return Specification<MCodmstEntity>
     */
    private Specification<MCodmstEntity> staffCodeLikeContains(final String staffCode) {

        if (StringUtils.isEmpty(staffCode)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.like(root.get("code1"), "%" + staffCode + "%");
        };
    }

    /**
     * 担当者コードの絞り込み検索条件(完全一致).
     *
     * @param staffCode 担当者コード
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> staffCodeMatchContains(final String staffCode) {

        if (StringUtils.isEmpty(staffCode)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("code1"),  staffCode);
        };
    }

    /**
     * 担当者名称の絞り込み検索条件(like検索).
     *
     * @param staffName 担当者名
     * @return Specification<MCodmstEntity>
     */
    private Specification<MCodmstEntity> staffNameLikeContains(final String staffName) {

        if (StringUtils.isEmpty(staffName)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.like(root.get("item2"), "%" + staffName + "%");
        };
    }

    /**
     * 職種の絞り込み検索条件(IN検索).
     * 検索タイプが3(コード完全一致)の場合は検索条件から職種を除外.
     *
     * @param staffType 担当者区分
     * @param searchType 検索タイプ
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> staffTypeInContains(final Integer staffType, final String searchType) {

        if (StringUtils.isEmpty(staffType) || MCodmstSearchType.CODE.getValue().equals(searchType)) {
            return null;
        }

        List<String> occupations = new ArrayList<>();   // 職種コードリスト

        switch (StaffType.convertToType(staffType)) {
        case PRODUCTION:
            occupations.add(OccupationType.DB.getValue());
            occupations.add(OccupationType.PRODUCTION.getValue());
            occupations.add(OccupationType.BUYER.getValue());
            occupations.add(OccupationType.MD.getValue());
            occupations.add(OccupationType.QUALITY.getValue());
            occupations.add(OccupationType.DEVELOPER.getValue());
            break;
        default:    // 絞り込みなし
            return null;
        }

        return (root, query, cb) -> {
            return cb.trim(root.get("item7")).in(occupations);
        };
    }


    /**
     * ブランドコードの絞り込み検索条件(完全一致).
     * 検索タイプが3(コード完全一致)の場合は検索条件からブランドコードを除外.
     *
     * @param brandCode ブランドコード
     * @param searchType 検索タイプ
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> brandContains(final String brandCode, final String searchType) {

        String divisionCode = "";

        // ブランド絞り込みのため事業部を取得する
        if (StringUtils.isEmpty(brandCode) || MCodmstSearchType.CODE.getValue().equals(searchType)) {
            return null;
        }

        List<MCodmstEntity> mCodmstEntityList = mCodmstRepository.findByTblidAndCode1OrderById(
                MCodmstTblIdType.BRAND.getValue(),
                brandCode,
                PageRequest.of(0, 1)).getContent();
        if (mCodmstEntityList.size() > 0) {
            // 事業部を取得
            divisionCode = mCodmstEntityList.get(0).getItem10();
        }

        // 事業部のデータがなければ絞り込みしない
        if (divisionCode == null || "".equals(divisionCode.trim())) {
            return null;
        }

        // final定義じゃないとラムダ式の中で使えないので、取得した事業部を再定義
        final String fDivisionCode = divisionCode;
        return (root, query, cb) -> {
            return cb.equal(root.get("item6"), fDivisionCode);
        };
    }
}
